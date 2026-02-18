package stark.dataworks.coderaider.gundam.core.runtime;

import java.util.List;
import java.util.stream.Collectors;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.agent.IAgentRegistry;
import stark.dataworks.coderaider.gundam.core.context.IContextBuilder;
import stark.dataworks.coderaider.gundam.core.hook.HookManager;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmOptions;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.model.Role;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.IToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * DefaultStepEngine implements single-step execution that binds model calls, tool calls, and memory updates.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
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
     * Performs default step engine as part of DefaultStepEngine runtime responsibilities.
     * @param llmClient The llm client used by this operation.
     * @param toolRegistry The tool registry used by this operation.
     * @param agentRegistry The agent registry used by this operation.
     * @param contextBuilder The context builder used by this operation.
     * @param hooks The hooks used by this operation.
     */
    public DefaultStepEngine(ILlmClient llmClient,
                             IToolRegistry toolRegistry,
                             IAgentRegistry agentRegistry,
                             IContextBuilder contextBuilder,
                             HookManager hooks)
    {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
        this.agentRegistry = agentRegistry;
        this.contextBuilder = contextBuilder;
        this.hooks = hooks;
    }

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param context The context used by this operation.
     * @param userInput The user input used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public AgentRunResult run(ExecutionContext context, String userInput)
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

            LlmResponse response = llmClient.chat(new LlmRequest(
                currentAgent.definition().getModel(),
                messages,
                tools,
                new LlmOptions(0.2, 512)));

            context.getTokenUsageTracker().add(response.getTokenUsage());

            if (!response.getContent().isBlank())
            {
                context.getMemory().append(new Message(Role.ASSISTANT, response.getContent()));
            }

            if (response.getHandoffAgentId().isPresent())
            {
                String handoffId = response.getHandoffAgentId().get();
                IAgent nextAgent = agentRegistry.get(handoffId)
                    .orElseThrow(() -> new IllegalStateException("Handoff target not found: " + handoffId));
                context.setAgent(nextAgent);
                userInput = null;
                continue;
            }

            if (!response.getToolCalls().isEmpty())
            {
                for (ToolCall toolCall : response.getToolCalls())
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
                response.getContent(),
                context.getTokenUsageTracker().snapshot(),
                context.getAgent().definition().getId());
        }

        hooks.afterRun(context);
        return new AgentRunResult(
            "Stopped: max steps reached",
            context.getTokenUsageTracker().snapshot(),
            context.getAgent().definition().getId());
    }
}
