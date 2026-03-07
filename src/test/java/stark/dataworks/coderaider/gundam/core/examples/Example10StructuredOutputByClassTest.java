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
 * 10) Structured output by declaring Java type from developer side.
 * 
 * Usage: java Example10StructuredOutputByClass [provider] [model] [apiKey] [prompt]
 * - provider: Provider type - "modelscope" (default) or "volcengine" (Seed/Doubao)
 * - model: Model name (default: Qwen/Qwen3-4B for modelscope, doubao-seed-1-6-251015 for volcengine)
 * - apiKey: API key (default: MODEL_SCOPE_API_KEY for modelscope, VOLCENGINE_API_KEY for volcengine)
 * - prompt: User prompt (default: "Generate a JSON summary of a sprint plan.")
 */
public class Example10StructuredOutputByClassTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String provider = "modelscope";
        String model = getDefaultModel(provider);
        String apiKey = getApiKey(provider, env);
        String prompt = "Generate a JSON summary of a sprint plan.";

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: API key is required for provider: " + provider);
            System.err.println("Set " + getApiKeyEnvVar(provider) + " environment variable or pass as third argument.");
            System.exit(1);
        }

        ILlmClient llmClient = createLlmClient(provider, apiKey, model);

        AgentDefinition definition = new AgentDefinition();
        definition.setId("structured-by-class");
        definition.setName("Structured By Class");
        definition.setModel(model);
        definition.setSystemPrompt("Generate a JSON object with fields: title (string), priority (number), blocked (boolean). Return only the JSON, nothing else.");
//        definition.setModelReasoning(Map.of("effort", "low"));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(definition);

        AgentRunner runner = AgentRunner.builder()
            .llmClient(llmClient)
            .toolRegistry(new ToolRegistry())
            .agentRegistry(agentRegistry)
            .eventPublisher(createConsoleStreamingPublisher())
            .build();

        RunConfiguration config = new RunConfiguration(12, null, 0.2, 512, "auto", "json_object", Map.of());
        ContextResult result = runner.chatClient("structured-by-class").prompt().user(prompt).runConfiguration(config).runHooks(ExampleSupport.noopHooks()).outputType(SprintSummary.class).call().contextResult();
        System.out.println("\nFinal output: " + result.getFinalOutput());
        if (!result.getItems().isEmpty())
        {
            Object payload = result.getItems().get(result.getItems().size() - 1).getMetadata();
            System.out.println("Structured output payload: " + payload);
        }
        System.out.println("Total token usage: " + result.getUsage().getTotalTokens() + " (input: " + result.getUsage().getInputTokens() + ", output: " + result.getUsage().getOutputTokens() + ")");
    }

    private static ILlmClient createLlmClient(String provider, String apiKey, String model)
    {
        return switch (provider.toLowerCase())
        {
            case "volcengine", "seed", "doubao" -> new SeedLlmClient(apiKey, model);
            default -> new ModelScopeLlmClient(apiKey, model, false);
        };
    }

    private static String getDefaultModel(String provider)
    {
        return switch (provider.toLowerCase())
        {
            case "volcengine", "seed", "doubao" -> "doubao-seed-1-6-251015";
            default -> "Qwen/Qwen3-4B";
        };
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
        return switch (provider.toLowerCase())
        {
            case "volcengine", "seed", "doubao" -> "VOLCENGINE_API_KEY";
            default -> "MODEL_SCOPE_API_KEY";
        };
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        return ExampleStreamingPublishers.reasoningAndText();
    }

    private static final class SprintSummary
    {
        private String title;
        private int priority;
        private boolean blocked;
    }
}
