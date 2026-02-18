package stark.dataworks.coderaider.runtime;

import java.util.List;
import java.util.stream.Collectors;
import stark.dataworks.coderaider.agent.IAgent;
import stark.dataworks.coderaider.agent.IAgentRegistry;
import stark.dataworks.coderaider.context.IContextBuilder;
import stark.dataworks.coderaider.hook.HookManager;
import stark.dataworks.coderaider.llmspi.ILlmClient;
import stark.dataworks.coderaider.llmspi.LlmOptions;
import stark.dataworks.coderaider.llmspi.LlmRequest;
import stark.dataworks.coderaider.llmspi.LlmResponse;
import stark.dataworks.coderaider.model.Message;
import stark.dataworks.coderaider.model.Role;
import stark.dataworks.coderaider.model.ToolCall;
import stark.dataworks.coderaider.tool.ITool;
import stark.dataworks.coderaider.tool.IToolRegistry;
import stark.dataworks.coderaider.tool.ToolDefinition;

public class DefaultStepEngine implements IStepEngine {
    private final ILlmClient llmClient;
    private final IToolRegistry toolRegistry;
    private final IAgentRegistry agentRegistry;
    private final IContextBuilder contextBuilder;
    private final HookManager hooks;

    public DefaultStepEngine(ILlmClient llmClient,
                             IToolRegistry toolRegistry,
                             IAgentRegistry agentRegistry,
                             IContextBuilder contextBuilder,
                             HookManager hooks) {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
        this.agentRegistry = agentRegistry;
        this.contextBuilder = contextBuilder;
        this.hooks = hooks;
    }

    @Override
    public AgentRunResult run(ExecutionContext context, String userInput) {
        hooks.beforeRun(context);
        while (context.getCurrentStep() < context.getAgent().definition().getMaxSteps()) {
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

            if (!response.getContent().isBlank()) {
                context.getMemory().append(new Message(Role.ASSISTANT, response.getContent()));
            }

            if (response.getHandoffAgentId().isPresent()) {
                String handoffId = response.getHandoffAgentId().get();
                IAgent nextAgent = agentRegistry.get(handoffId)
                    .orElseThrow(() -> new IllegalStateException("Handoff target not found: " + handoffId));
                context.setAgent(nextAgent);
                userInput = null;
                continue;
            }

            if (!response.getToolCalls().isEmpty()) {
                for (ToolCall toolCall : response.getToolCalls()) {
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
