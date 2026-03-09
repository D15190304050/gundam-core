package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 1) How to create & run a single simple agent with streaming output.
 * <p>
 * Usage: java Example01SingleSimpleAgent [model] [apiKey] [prompt]
 * - model: ModelScope model name (default: Qwen/Qwen3-4B)
 * - apiKey: Your ModelScope API key (required, or set MODEL_SCOPE_API_KEY env var)
 * - prompt: User prompt (default: "Introduce generic-agent-core in one sentence.")
 */
public class Example01SingleSimpleAgentTest
{
    //    public static void main(String[] args)
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        String prompt = "How do I cook braised beef noodles? Please provide detailed instructions.";

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.err.println("Set MODEL_SCOPE_API_KEY environment variable or pass as second argument.");
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
            .llmClient(new ModelScopeLlmClient(apiKey, model))
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
