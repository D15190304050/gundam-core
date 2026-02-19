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
 * Example showing model-directed handoff from triage agent to specialist agent.
 */
public class HandoffAgentExample
{
    public static void main(String[] args)
    {
        AgentDefinition triage = new AgentDefinition();
        triage.setId("triage");
        triage.setName("Triage");
        triage.setModel("gpt-4.1-mini");
        triage.setSystemPrompt("Route billing questions to billing-agent.");
        triage.setHandoffAgentIds(List.of("billing-agent"));

        AgentDefinition billing = new AgentDefinition();
        billing.setId("billing-agent");
        billing.setName("Billing");
        billing.setModel("gpt-4.1-mini");
        billing.setSystemPrompt("Handle billing requests.");

        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(triage));
        registry.register(new Agent(billing));

        AgentRunner runner = new AgentRunner(
            new HandoffClient(),
            new ToolRegistry(),
            registry,
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

        RunResult result = runner.run(registry.get("triage").orElseThrow(), "I need help with billing", RunConfiguration.defaults(), new NoopRunHooks());
        System.out.println("Final agent: " + result.getFinalAgentId());
        System.out.println("Output: " + result.getFinalOutput());
    }

    private static class NoopRunHooks implements IRunHooks
    {
    }

    private static class HandoffClient implements ILlmClient
    {
        @Override
        public LlmResponse chat(LlmRequest request)
        {
            String systemPrompt = request.getMessages().get(0).getContent().toLowerCase();
            if (systemPrompt.contains("route billing"))
            {
                return new LlmResponse("", List.of(), "billing-agent", new TokenUsage(6, 2));
            }
            return new LlmResponse("Your invoice has been updated.", List.of(), null, new TokenUsage(8, 12));
        }
    }
}
