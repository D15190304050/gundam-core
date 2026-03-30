package stark.dataworks.coderaider.genericagent.core.llmspi.adapter;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.genericagent.core.agent.AgentDefinition;
import stark.dataworks.coderaider.genericagent.core.agent.AgentRegistry;
import stark.dataworks.coderaider.genericagent.core.examples.ExampleStreamingPublishers;
import stark.dataworks.coderaider.genericagent.core.examples.ExampleSupport;
import stark.dataworks.coderaider.genericagent.core.runner.AgentRunner;
import stark.dataworks.coderaider.genericagent.core.runner.RunConfiguration;
import stark.dataworks.coderaider.genericagent.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.genericagent.core.tool.ToolRegistry;

public class ExampleMiniMaxLlmClientTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String model = "MiniMax-M2.7";
        String apiKey = env.get("MINIMAX_API_KEY", System.getenv("MINIMAX_API_KEY"));
        String prompt = "Introduce yourself in one sentence.";

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: MiniMax API key is required.");
            System.err.println("Set MINIMAX_API_KEY environment variable or add to .env.local file.");
            System.exit(1);
        }

        AgentDefinition agentDef = new AgentDefinition();
        agentDef.setId("simple-agent");
        agentDef.setName("Simple Agent");
        agentDef.setModel(model);
        agentDef.setSystemPrompt("You are a concise assistant.");

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(agentDef);

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new MiniMaxLlmClient(apiKey, model))
            .toolRegistry(new ToolRegistry())
            .agentRegistry(agentRegistry)
            .eventPublisher(createConsoleStreamingPublisher())
            .build();

        System.out.print("Streaming output: ");
        String output = runner.chatClient("simple-agent")
            .prompt()
            .user(prompt)
            .runConfiguration(RunConfiguration.defaults())
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .content();
        System.out.println();
        System.out.println("Final output: " + output);
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        return ExampleStreamingPublishers.textOnly();
    }
}
