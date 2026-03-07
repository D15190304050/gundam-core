package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 08) Create agent definition from JSON then run it through Spring-AI style chat API.
 */
public class Example08AgentDefinitionFromJsonTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        String prompt = "Please briefly introduce GUNDAM-core.";

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.exit(1);
        }

        String definitionJson = """
            {
              "id": "simple-agent-json",
              "name": "Simple Agent JSON",
              "model": "%s",
              "systemPrompt": "You are a concise assistant."
            }
            """.formatted(model);

        AgentDefinition agentDef = AgentDefinition.fromJson(definitionJson);

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(agentDef);

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(new ToolRegistry())
            .agentRegistry(agentRegistry)
            .build();

        String output = runner.chatClient("simple-agent-json")
            .prompt()
            .user(prompt)
            .runConfiguration(RunConfiguration.defaults())
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .content();

        System.out.println("Final output: " + output);
    }
}
