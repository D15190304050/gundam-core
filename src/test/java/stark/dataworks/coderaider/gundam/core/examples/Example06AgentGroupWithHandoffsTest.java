package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.util.List;

import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.event.RunEventType;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.streaming.IRunEventListener;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 6) How to create a group of agents with handoffs, with at least 3 agents, with streaming output.
 * 
 * Usage: java Example06AgentGroupWithHandoffs [model] [apiKey] [query]
 * - model: ModelScope model name (default: Qwen/Qwen3-4B)
 * - apiKey: Your ModelScope API key (required, or set MODEL_SCOPE_API_KEY env var)
 * - query: User query (default: "Need a migration plan for next week.")
 * 
 * Note: Agent handoffs require the LLM to return specific handoff markers.
 * The agents are configured with handoff relationships:
 * - triage -> planner, support
 * - planner -> support
 * - support (final agent, no handoffs)
 */
public class Example06AgentGroupWithHandoffsTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        String query = "Need a migration plan for next week.";

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.err.println("Set MODEL_SCOPE_API_KEY environment variable or pass as second argument.");
            System.exit(1);
        }

        AgentDefinition triage = new AgentDefinition();
        triage.setId("triage");
        triage.setName("Triage Agent");
        triage.setModel(model);
        triage.setSystemPrompt("You are a triage agent. Analyze the user's request and route to the appropriate specialist agent. " +
            "For planning tasks, handoff to 'planner'. For support questions, handoff to 'support'. " +
            "To handoff, respond with 'handoff: <agent_id>' where agent_id is 'planner' or 'support'.");
        triage.setHandoffAgentIds(List.of("planner", "support"));

        AgentDefinition planner = new AgentDefinition();
        planner.setId("planner");
        planner.setName("Planner Agent");
        planner.setModel(model);
        planner.setSystemPrompt("You are a planning agent. Create detailed plans for the user's request. " +
            "After creating a plan, handoff to 'support' for final delivery. " +
            "To handoff, respond with 'handoff: support'.");
        planner.setHandoffAgentIds(List.of("support"));

        AgentDefinition support = new AgentDefinition();
        support.setId("support");
        support.setName("Support Agent");
        support.setModel(model);
        support.setSystemPrompt("You are a support agent. Deliver the final response to the user in a friendly and helpful manner. " +
            "Summarize any plans or information provided by previous agents.");

        AgentRegistry registry = new AgentRegistry();
        registry.register(triage);
        registry.register(planner);
        registry.register(support);

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(new ToolRegistry())
            .agentRegistry(registry)
            .eventPublisher(createConsoleStreamingPublisher())
            .build();

        System.out.print("Streaming output: ");
        ContextResult result = runner.chatClient("triage").prompt().user(query).runConfiguration(RunConfiguration.defaults()).runHooks(ExampleSupport.noopHooks()).call().contextResult();
        System.out.println();

        System.out.println("FinalAgent=" + result.getFinalAgentId());
        System.out.println("Output=" + result.getFinalOutput());
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        return ExampleStreamingPublishers.textOnly();
    }
}
