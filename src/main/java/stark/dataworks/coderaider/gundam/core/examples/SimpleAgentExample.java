package stark.dataworks.coderaider.gundam.core.examples;

import java.util.List;

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
import stark.dataworks.coderaider.gundam.core.output.OutputSchemaRegistry;
import stark.dataworks.coderaider.gundam.core.output.OutputValidator;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.IRunHooks;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.session.InMemorySessionStore;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tracing.NoopTraceProvider;

/**
 * Minimal runnable example for a text-only agent.
 */
public class SimpleAgentExample
{
    public static void main(String[] args)
    {
        String model = args.length > 0 ? args[0] : "gpt-4.1-mini";
        String baseUrl = args.length > 1 ? args[1] : "https://your-base-url";
        String apiKey = args.length > 2 ? args[2] : "YOUR_API_KEY";
        String prompt = args.length > 3 ? args[3] : "Say hello from GUNDAM-core";

        AgentDefinition definition = new AgentDefinition();
        definition.setId("assistant");
        definition.setName("Assistant");
        definition.setModel(model);
        definition.setSystemPrompt("You are a concise assistant.");

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(definition));

        ILlmClient client = new PlaceholderLlmClient(baseUrl, apiKey);
        AgentRunner runner = new AgentRunner(
            client,
            new ToolRegistry(),
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

        RunResult result = runner.run(agentRegistry.get("assistant").orElseThrow(), prompt, RunConfiguration.defaults(), new NoopRunHooks());
        System.out.println("Model: " + model);
        System.out.println("Base URL: " + baseUrl + " (placeholder)");
        System.out.println("Output: " + result.getFinalOutput());
    }

    private static class NoopRunHooks implements IRunHooks
    {
    }

    private record PlaceholderLlmClient(String baseUrl, String apiKey) implements ILlmClient
    {
        @Override
        public LlmResponse chat(LlmRequest request)
        {
            String userMessage = request.getMessages().isEmpty() ? "" : request.getMessages().get(request.getMessages().size() - 1).getContent();
            String output = "[placeholder response] baseUrl=" + baseUrl + ", apiKey=" + mask(apiKey) + ", prompt=" + userMessage;
            return new LlmResponse(output, List.of(), null, new TokenUsage(10, 20));
        }

        private static String mask(String key)
        {
            if (key == null || key.length() < 4)
            {
                return "****";
            }
            return "****" + key.substring(key.length() - 4);
        }
    }
}
