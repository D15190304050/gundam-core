package stark.dataworks.coderaider.gundam.core.examples;

import java.util.List;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 5) How to create a group of agents with handoffs, with at least 3 agents.
 */
public class Example05AgentGroupWithHandoffs
{
    public static void main(String[] args)
    {
        AgentDefinition triage = new AgentDefinition();
        triage.setId("triage");
        triage.setName("Triage Agent");
        triage.setModel("gpt-4.1-mini");
        triage.setSystemPrompt("Route to specialist agents.");
        triage.setHandoffAgentIds(List.of("planner", "support"));

        AgentDefinition planner = new AgentDefinition();
        planner.setId("planner");
        planner.setName("Planner Agent");
        planner.setModel("gpt-4.1-mini");
        planner.setSystemPrompt("Make plans then handoff to support for final response.");
        planner.setHandoffAgentIds(List.of("support"));

        AgentDefinition support = new AgentDefinition();
        support.setId("support");
        support.setName("Support Agent");
        support.setModel("gpt-4.1-mini");
        support.setSystemPrompt("Deliver final customer-friendly answer.");

        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(triage));
        registry.register(new Agent(planner));
        registry.register(new Agent(support));

        AgentRunner runner = ExampleSupport.runner(new HandoffChainClient(), new ToolRegistry(), registry, null);
        RunResult result = runner.run(registry.get("triage").orElseThrow(), "Need a migration plan for next week.", RunConfiguration.defaults(), ExampleSupport.noopHooks());

        System.out.println("FinalAgent=" + result.getFinalAgentId());
        System.out.println("Output=" + result.getFinalOutput());
    }

    private static class HandoffChainClient implements ILlmClient
    {
        @Override
        public LlmResponse chat(LlmRequest request)
        {
            String system = request.getMessages().get(0).getContent().toLowerCase();
            if (system.contains("route to specialist"))
            {
                return new LlmResponse("", List.of(), "planner", new TokenUsage(6, 2));
            }
            if (system.contains("make plans"))
            {
                return new LlmResponse("", List.of(), "support", new TokenUsage(6, 2));
            }
            return new LlmResponse("Here is a staged migration plan with owners and checkpoints.", List.of(), null, new TokenUsage(9, 16));
        }
    }
}
