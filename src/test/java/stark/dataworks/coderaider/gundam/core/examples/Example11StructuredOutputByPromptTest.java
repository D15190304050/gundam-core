package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.event.RunEventType;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.SeedLlmClient;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.streaming.IRunEventListener;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 11) Structured output by prompting the model with user-defined schema and JSON mode.
 * 
 * Usage: java Example11StructuredOutputByPrompt [provider] [model] [apiKey] [topic]
 * - provider: Provider type - "modelscope" (default) or "volcengine" (Seed/Doubao)
 * - model: Model name (default: Qwen/Qwen3-4B for modelscope, doubao-seed-1-6-251015 for volcengine)
 * - apiKey: API key (default: MODEL_SCOPE_API_KEY for modelscope, VOLCENGINE_API_KEY for volcengine)
 * - topic: Topic for JSON generation (default: "Java")
 */
public class Example11StructuredOutputByPromptTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String provider = "modelscope";
        String model = getDefaultModel(provider);
        String apiKey = getApiKey(provider, env);
        String topic = "Java";

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: API key is required for provider: " + provider);
            System.err.println("Set " + getApiKeyEnvVar(provider) + " environment variable or pass as third argument.");
            System.exit(1);
        }

        ILlmClient llmClient = createLlmClient(provider, apiKey, model);

        AgentDefinition definition = new AgentDefinition();
        definition.setId("structured-by-prompt");
        definition.setName("Structured By Prompt");
        definition.setModel(model);
        definition.setSystemPrompt("Always obey user schema exactly.");

        AgentRegistry registry = new AgentRegistry();
        registry.register(definition);

        AgentRunner runner = AgentRunner.builder()
            .llmClient(llmClient)
            .toolRegistry(new ToolRegistry())
            .agentRegistry(registry)
            .eventPublisher(createConsoleStreamingPublisher())
            .build();

        String prompt = "Return a JSON response following this schema: {\"topic\":\"string\",\"score\":\"number\",\"tags\":[\"string1\",\"string2\"]}. Topic = " + topic + ".";
        RunConfiguration config = new RunConfiguration(8, null, 0.2, 512, "auto", "json_object", Map.of());
        ContextResult result = runner.chatClient("structured-by-prompt").prompt().user(prompt).runConfiguration(config).runHooks(ExampleSupport.noopHooks()).call().contextResult();

        System.out.println("\nPrompt-defined JSON: " + result.getFinalOutput());
        System.out.println("Total token usage: " + result.getUsage().getTotalTokens() + " (input: " + result.getUsage().getInputTokens() + ", output: " + result.getUsage().getOutputTokens() + ")");
    }

    private static ILlmClient createLlmClient(String provider, String apiKey, String model)
    {
        switch (provider.toLowerCase())
        {
            case "volcengine":
            case "seed":
            case "doubao":
                return new SeedLlmClient(apiKey, model);
            case "modelscope":
            default:
                return new ModelScopeLlmClient(apiKey, model, false);
        }
    }

    private static String getDefaultModel(String provider)
    {
        switch (provider.toLowerCase())
        {
            case "volcengine":
            case "seed":
            case "doubao":
                return "doubao-seed-1-6-251015";
            case "modelscope":
            default:
                return "Qwen/Qwen3-4B";
        }
    }

    private static String getApiKey(String provider, Dotenv env)
    {
        switch (provider.toLowerCase())
        {
            case "volcengine":
            case "seed":
            case "doubao":
                return env.get("VOLCENGINE_API_KEY", System.getenv("VOLCENGINE_API_KEY"));
            case "modelscope":
            default:
                return env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        }
    }

    private static String getApiKeyEnvVar(String provider)
    {
        switch (provider.toLowerCase())
        {
            case "volcengine":
            case "seed":
            case "doubao":
                return "VOLCENGINE_API_KEY";
            case "modelscope":
            default:
                return "MODEL_SCOPE_API_KEY";
        }
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        return ExampleStreamingPublishers.reasoningAndText();
    }
}
