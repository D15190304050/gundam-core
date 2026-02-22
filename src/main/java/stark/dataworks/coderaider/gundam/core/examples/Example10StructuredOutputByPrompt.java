package stark.dataworks.coderaider.gundam.core.examples;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.event.RunEventType;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.SeedLlmClient;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.streaming.IRunEventListener;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * Structured output by prompting the model with user-defined schema and JSON mode.
 * 
 * Usage: java Example10StructuredOutputByPrompt [provider] [model] [apiKey] [topic]
 * - provider: Provider type - "modelscope" (default) or "volcengine" (Seed/Doubao)
 * - model: Model name (default: Qwen/Qwen3-4B for modelscope, doubao-seed-1-6-251015 for volcengine)
 * - apiKey: API key (default: MODEL_SCOPE_API_KEY for modelscope, VOLCENGINE_API_KEY for volcengine)
 * - topic: Topic for JSON generation (default: "Java")
 */
public class Example10StructuredOutputByPrompt
{
    public static void main(String[] args)
    {
        String provider = args.length > 0 ? args[0] : "volcengine";
        String model = args.length > 1 ? args[1] : getDefaultModel(provider);
        String apiKey = args.length > 2 ? args[2] : getApiKey(provider);
        String topic = args.length > 3 ? args[3] : "Java";

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
        definition.setModelReasoning(Map.of("effort", "low"));

        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(definition));

        AgentRunner runner = ExampleSupport.runnerWithPublisher(
            llmClient,
            new ToolRegistry(),
            registry,
            null,
            createConsoleStreamingPublisher());

        String prompt = "Return only JSON with fields: topic(string), score(number), tags(array). Topic = " + topic + ".";
        RunConfiguration config = new RunConfiguration(8, null, 0.2, 512, "auto", "json_object", Map.of());
        RunResult result = runner.runStreamed(registry.get("structured-by-prompt").orElseThrow(), prompt, config, ExampleSupport.noopHooks());

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
                return new ModelScopeLlmClient(apiKey, model);
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

    private static String getApiKey(String provider)
    {
        switch (provider.toLowerCase())
        {
            case "volcengine":
            case "seed":
            case "doubao":
                return System.getenv("VOLCENGINE_API_KEY");
            case "modelscope":
            default:
                return System.getenv("MODEL_SCOPE_API_KEY");
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
        RunEventPublisher publisher = new RunEventPublisher();
        publisher.subscribe(new IRunEventListener()
        {
            @Override
            public void onEvent(RunEvent event)
            {
                if (event.getType() == RunEventType.MODEL_REASONING_DELTA)
                {
                    String delta = (String) event.getAttributes().get("delta");
                    if (delta != null)
                    {
                        System.out.print("[reasoning] " + delta + "\n");
                    }
                }
                else if (event.getType() == RunEventType.MODEL_RESPONSE_DELTA)
                {
                    String delta = (String) event.getAttributes().get("delta");
                    if (delta != null)
                    {
                        System.out.print(delta);
                        System.out.flush();
                    }
                }
            }
        });
        return publisher;
    }
}
