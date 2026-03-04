package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.builtin.AgentTool;

import java.util.List;

/**
 * 26) How to use one agent as a tool for another agent, with streaming output.
 */
public class Example26AgentAsToolTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.exit(1);
        }

        AgentDefinition orchestrator = new AgentDefinition();
        orchestrator.setId("orchestrator-agent");
        orchestrator.setName("Orchestrator Agent");
        orchestrator.setModel(model);
        orchestrator.setSystemPrompt("You are a project assistant. You MUST call tool 'delegated_research' exactly once, then summarize the returned content in Chinese bullet points.");
        orchestrator.setToolNames(List.of("delegated_research"));

        AgentDefinition specialist = new AgentDefinition();
        specialist.setId("specialist-agent");
        specialist.setName("Specialist Agent");
        specialist.setModel(model);
        specialist.setSystemPrompt("You are a specialist researcher. Return concise and factual findings in Chinese.");

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(orchestrator));
        agentRegistry.register(new Agent(specialist));

        ToolRegistry toolRegistry = new ToolRegistry();

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(createConsoleStreamingPublisher())
            .build();

        toolRegistry.register(new AgentTool(
            new ToolDefinition(
                "delegated_research",
                "Delegate a focused research task to specialist-agent and return the specialist answer.",
                List.of(new ToolParameterSchema("task", "string", true, "The detailed research task for specialist agent"))),
            runner,
            "specialist-agent",
            RunConfiguration.defaults()));

        System.out.print("Streaming output: ");
        String output = runner.chatClient("orchestrator-agent")
            .prompt()
            .user("Please research the key risks of upgrading a TypeScript project to the latest LTS Node.js and provide 3 recommendations.")
            .runConfiguration(RunConfiguration.defaults())
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .content();
        System.out.println();
        System.out.println("Final output: " + output);
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        return ExampleStreamingPublishers.textWithToolLifecycle("");
    }
}
