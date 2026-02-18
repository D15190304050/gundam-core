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
 * Class DefaultStepEngine.
 */

public class DefaultStepEngine implements IStepEngine
{
    /**
     * Field llmClient.
     */
    private final ILlmClient llmClient;
    /**
     * Field toolRegistry.
     */
    private final IToolRegistry toolRegistry;
    /**
     * Field agentRegistry.
     */
    private final IAgentRegistry agentRegistry;
    /**
     * Field contextBuilder.
     */
    private final IContextBuilder contextBuilder;
    /**
     * Field hooks.
     */
    private final HookManager hooks;
    /**
     * Creates a new DefaultStepEngine instance.
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
     * Executes run.
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
