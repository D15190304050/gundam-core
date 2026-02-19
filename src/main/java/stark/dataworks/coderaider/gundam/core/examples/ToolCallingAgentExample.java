package stark.dataworks.coderaider.gundam.core.examples;

import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.approval.AllowAllToolApprovalPolicy;
import stark.dataworks.coderaider.gundam.core.context.DefaultContextBuilder;
import stark.dataworks.coderaider.gundam.core.guardrail.GuardrailEngine;
import stark.dataworks.coderaider.gundam.core.handoff.HandoffRouter;
import stark.dataworks.coderaider.gundam.core.hook.HookManager;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
import stark.dataworks.coderaider.gundam.core.output.OutputSchemaRegistry;
import stark.dataworks.coderaider.gundam.core.output.OutputValidator;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.IRunHooks;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.session.InMemorySessionStore;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tracing.NoopTraceProvider;

/**
 * Runnable example with one tool call turn followed by final output.
 */
public class ToolCallingAgentExample
{
    public static void main(String[] args)
    {
        String city = args.length > 0 ? args[0] : "Tokyo";

        AgentDefinition definition = new AgentDefinition();
        definition.setId("weather-agent");
        definition.setName("Weather Agent");
        definition.setModel("gpt-4.1-mini");
        definition.setSystemPrompt("Use the weather tool when asked about weather.");
        definition.setToolNames(List.of("get_weather"));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(definition));

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition("get_weather", "Returns mock weather", List.of());
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                return "Sunny in " + input.getOrDefault("city", "Unknown") + ", 25C";
            }
        });

        AgentRunner runner = new AgentRunner(
            new TwoStepToolClient(),
            toolRegistry,
            agentRegistry,
            new DefaultContextBuilder(),
            new HookManager(),
            new GuardrailEngine(),
            new HandoffRouter(),
            new InMemorySessionStore(),
            new NoopTraceProvider(),
            new AllowAllToolApprovalPolicy(),
            new OutputSchemaRegistry(),
            new OutputValidator(),
            new RunEventPublisher());

        RunResult result = runner.run(agentRegistry.get("weather-agent").orElseThrow(), "What's the weather in " + city + "?", RunConfiguration.defaults(), new NoopRunHooks());
        System.out.println(result.getFinalOutput());
    }

    private static class NoopRunHooks implements IRunHooks
    {
    }

    private static class TwoStepToolClient implements ILlmClient
    {
        @Override
        public LlmResponse chat(LlmRequest request)
        {
            boolean hasToolMessage = request.getMessages().stream().anyMatch(message -> message.getRole().name().equals("TOOL"));
            if (!hasToolMessage)
            {
                return new LlmResponse("", List.of(new ToolCall("get_weather", Map.of("city", "Tokyo"))), null, new TokenUsage(10, 5));
            }
            return new LlmResponse("The forecast says sunny and warm.", List.of(), null, new TokenUsage(8, 12));
        }
    }
}
