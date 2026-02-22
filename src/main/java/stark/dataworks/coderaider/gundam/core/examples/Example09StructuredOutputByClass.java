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
 * Structured output by declaring Java type from developer side.
 * 
 * Usage: java Example09StructuredOutputByClass [provider] [model] [apiKey] [prompt]
 * - provider: Provider type - "modelscope" (default) or "volcengine" (Seed/Doubao)
 * - model: Model name (default: Qwen/Qwen3-4B for modelscope, doubao-seed-1-6-251015 for volcengine)
 * - apiKey: API key (default: MODEL_SCOPE_API_KEY for modelscope, VOLCENGINE_API_KEY for volcengine)
 * - prompt: User prompt (default: "Generate a JSON summary of a sprint plan.")
 */
public class Example09StructuredOutputByClass
{
    public static void main(String[] args)
    {
        String provider = args.length > 0 ? args[0] : "volcengine";
        String model = args.length > 1 ? args[1] : getDefaultModel(provider);
        String apiKey = args.length > 2 ? args[2] : getApiKey(provider);
        String prompt = args.length > 3 ? args[3] : "Generate a JSON summary of a sprint plan.";

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
        definition.setSystemPrompt("Return concise structured summaries.");
        definition.setModelReasoning(Map.of("effort", "low"));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(definition));

        AgentRunner runner = ExampleSupport.runnerWithPublisher(
            llmClient,
            new ToolRegistry(),
            agentRegistry,
            null,
            createConsoleStreamingPublisher());

        RunResult result = runner.runStreamed(agentRegistry.get("structured-by-class").orElseThrow(), prompt, RunConfiguration.defaults(), ExampleSupport.noopHooks(), SprintSummary.class);
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
            default -> new ModelScopeLlmClient(apiKey, model);
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
        return switch (provider.toLowerCase())
        {
            case "volcengine", "seed", "doubao" -> "VOLCENGINE_API_KEY";
            default -> "MODEL_SCOPE_API_KEY";
        };
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

    private static final class SprintSummary
    {
        private String title;
        private int priority;
        private boolean blocked;
    }
}
