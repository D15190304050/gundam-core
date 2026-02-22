package stark.dataworks.coderaider.gundam.core.examples;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * Demonstrates the AgentRunner builder API.
 */
public class Example11AgentRunnerBuilder
{
    public static void main(String[] args)
    {
        String model = args.length > 0 ? args[0] : "Qwen/Qwen3-4B";
        String apiKey = args.length > 1 ? args[1] : System.getenv("MODEL_SCOPE_API_KEY");

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.err.println("Set MODEL_SCOPE_API_KEY environment variable or pass as second argument.");
            System.exit(1);
        }

        AgentDefinition definition = new AgentDefinition();
        definition.setId("builder-agent");
        definition.setName("Builder Agent");
        definition.setModel(model);
        definition.setSystemPrompt("You are a helpful assistant.");

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(definition));

        AgentRunner runner = AgentRunner.builder(new ModelScopeLlmClient(apiKey, model), new ToolRegistry(), agentRegistry)
            .build();

        RunResult result = runner.run(new Agent(definition), "Explain why builder APIs are helpful.", RunConfiguration.defaults(), ExampleSupport.noopHooks());
        System.out.println(result.getFinalOutput());
    }
}
