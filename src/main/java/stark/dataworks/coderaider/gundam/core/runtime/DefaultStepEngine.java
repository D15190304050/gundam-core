package stark.dataworks.coderaider.gundam.core.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.agent.IAgentRegistry;
import stark.dataworks.coderaider.gundam.core.context.IContextBuilder;
import stark.dataworks.coderaider.gundam.core.hook.HookManager;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmOptions;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmStreamListener;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.model.Role;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.IToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * {@link DefaultStepEngine} implements single-step execution that binds model calls, tool calls, and memory updates.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
@AllArgsConstructor
public class DefaultStepEngine implements IStepEngine
{

    /**
     * Internal state for llm client; used while coordinating runtime behavior.
     */
    private final ILlmClient llmClient;

    /**
     * Internal state for tool registry; used while coordinating runtime behavior.
     */
    private final IToolRegistry toolRegistry;

    /**
     * Internal state for agent registry; used while coordinating runtime behavior.
     */
    private final IAgentRegistry agentRegistry;

    /**
     * Internal state for context builder; used while coordinating runtime behavior.
     */
    private final IContextBuilder contextBuilder;

    /**
     * Internal state for hooks; used while coordinating runtime behavior.
     */
    private final HookManager hooks;

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param context The context used by this operation.
     * @param userInput The user input used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public AgentRunResult run(ExecutionContext context, String userInput)
    {
        return runInternal(context, userInput, false);
    }

    /**
     * Runs the primary execution flow with streamed model deltas.
     * @param context The context used by this operation.
     * @param userInput The user input used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public AgentRunResult runStreamed(ExecutionContext context, String userInput)
    {
        return runInternal(context, userInput, true);
    }

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param context The context used by this operation.
     * @param userInput The user input used by this operation.
     * @param streamModelResponse The stream model response used by this operation.
     * @return The value produced by this operation.
     */
    private AgentRunResult runInternal(ExecutionContext context, String userInput, boolean streamModelResponse)
    {
        hooks.beforeRun(context);
        while (context.getCurrentStep() < context.getAgent().definition().getMaxSteps())
        {
            hooks.onStep(context);
            context.incrementStep();

            IAgent currentAgent = context.getAgent();
            List<Message> messages = contextBuilder.build(currentAgent, context.getMemory(), userInput);
            List<ToolDefinition> tools = currentAgent.definition().getToolNames().stream()
                .map(name -> toolRegistry.get(name)
                    .orElseThrow(() -> new IllegalStateException("Tool not found: " + name))
                    .definition())
                .collect(Collectors.toList());

            LlmRequest request = new LlmRequest(
                currentAgent.definition().getModel(),
                messages,
                tools,
                new LlmOptions(currentAgent.definition().getModelTemperature(),
                    currentAgent.definition().getModelMaxTokens(),
                    currentAgent.definition().getModelToolChoice(),
                    currentAgent.definition().getModelResponseFormat(),
                    currentAgent.definition().getModelProviderOptions()));

            StringBuilder streamedContent = new StringBuilder();
            List<ToolCall> streamedToolCalls = new ArrayList<>();
            TokenUsage[] streamedTokenUsage = new TokenUsage[1];
            String[] streamedHandoff = new String[1];
            LlmResponse[] streamedFinalResponse = new LlmResponse[1];

            LlmStreamListener streamListener = new LlmStreamListener()
            {
                @Override
                public void onDelta(String delta)
                {
                    streamedContent.append(delta);
                    hooks.onModelResponseDelta(context, delta);
                }

                @Override
                public void onToolCall(ToolCall toolCall)
                {
                    if (toolCall != null)
                    {
                        streamedToolCalls.add(toolCall);
                    }
                }

                @Override
                public void onTokenUsage(TokenUsage tokenUsage)
                {
                    if (tokenUsage != null)
                    {
                        streamedTokenUsage[0] = tokenUsage;
                    }
                }

                @Override
                public void onHandoff(String handoffAgentId)
                {
                    streamedHandoff[0] = handoffAgentId;
                }

                @Override
                public void onCompleted(LlmResponse response)
                {
                    streamedFinalResponse[0] = response;
                }
            };

            LlmResponse response = streamModelResponse
                ? llmClient.chatStream(request, streamListener)
                : llmClient.chat(request);

            LlmResponse effectiveResponse = mergeWithStreamedResponse(response,
                streamedFinalResponse[0],
                streamedContent.toString(),
                streamedToolCalls,
                streamedHandoff[0],
                streamedTokenUsage[0]);

            context.getTokenUsageTracker().add(effectiveResponse.getTokenUsage());

            if (!effectiveResponse.getContent().isBlank())
            {
                context.getMemory().append(new Message(Role.ASSISTANT, effectiveResponse.getContent()));
            }

            if (effectiveResponse.getHandoffAgentId().isPresent())
            {
                String handoffId = effectiveResponse.getHandoffAgentId().get();
                IAgent nextAgent = agentRegistry.get(handoffId)
                    .orElseThrow(() -> new IllegalStateException("Handoff target not found: " + handoffId));
                context.setAgent(nextAgent);
                userInput = null;
                continue;
            }

            if (!effectiveResponse.getToolCalls().isEmpty())
            {
                for (ToolCall toolCall : effectiveResponse.getToolCalls())
                {
                    ITool tool = toolRegistry.get(toolCall.getToolName())
                        .orElseThrow(() -> new IllegalStateException("Tool not found: " + toolCall.getToolName()));
                    hooks.beforeTool(toolCall.getToolName(), toolCall.getArguments());
                    String result = tool.execute(toolCall.getArguments());
                    hooks.afterTool(toolCall.getToolName(), result);
                    context.getMemory().append(new Message(Role.TOOL, toolCall.getToolName() + ": " + result));
                }
                userInput = null;
                continue;
            }

            hooks.afterRun(context);
            return new AgentRunResult(
                effectiveResponse.getContent(),
                context.getTokenUsageTracker().snapshot(),
                context.getAgent().definition().getId());
        }

        hooks.afterRun(context);
        return new AgentRunResult(
            "Stopped: max steps reached",
            context.getTokenUsageTracker().snapshot(),
            context.getAgent().definition().getId());
    }

    /**
     * Merges streamed signals with terminal response payload so tool calls and usage are not dropped.
     * @param response The response used by this operation.
     * @param streamedFinalResponse The streamed final response used by this operation.
     * @param streamedContent The streamed content used by this operation.
     * @param streamedToolCalls The streamed tool calls used by this operation.
     * @param streamedHandoff The streamed handoff used by this operation.
     * @param streamedTokenUsage The streamed token usage used by this operation.
     * @return The value produced by this operation.
     */
    private LlmResponse mergeWithStreamedResponse(LlmResponse response,
                                                  LlmResponse streamedFinalResponse,
                                                  String streamedContent,
                                                  List<ToolCall> streamedToolCalls,
                                                  String streamedHandoff,
                                                  TokenUsage streamedTokenUsage)
    {
        LlmResponse base = response != null ? response : streamedFinalResponse;
        if (base == null)
        {
            return new LlmResponse(
                streamedContent == null ? "" : streamedContent,
                streamedToolCalls == null ? List.of() : List.copyOf(streamedToolCalls),
                streamedHandoff,
                streamedTokenUsage == null ? new TokenUsage(0, 0) : streamedTokenUsage,
                "stop",
                Map.of());
        }

        String content = base.getContent();
        if ((content == null || content.isBlank()) && streamedContent != null && !streamedContent.isBlank())
        {
            content = streamedContent;
        }

        List<ToolCall> toolCalls = base.getToolCalls();
        if ((toolCalls == null || toolCalls.isEmpty()) && streamedToolCalls != null && !streamedToolCalls.isEmpty())
        {
            toolCalls = List.copyOf(streamedToolCalls);
        }

        String handoffAgentId = base.getHandoffAgentId().orElse(streamedHandoff);

        TokenUsage tokenUsage = base.getTokenUsage();
        if (tokenUsage == null)
        {
            tokenUsage = streamedTokenUsage;
        }
        else if (streamedTokenUsage != null && tokenUsage.getTotalTokens() == 0 && streamedTokenUsage.getTotalTokens() > 0)
        {
            tokenUsage = streamedTokenUsage;
        }
        if (tokenUsage == null)
        {
            tokenUsage = new TokenUsage(0, 0);
        }

        return new LlmResponse(
            content == null ? "" : content,
            toolCalls == null ? List.of() : toolCalls,
            handoffAgentId,
            tokenUsage,
            base.getFinishReason(),
            base.getStructuredOutput());
    }
}
