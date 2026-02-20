package stark.dataworks.coderaider.gundam.core.examples;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.event.RunEventType;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.streaming.IRunEventListener;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 6) How to stream reasoning and answer deltas separately.
 *
 * Usage: java Example06ReasoningStreaming [model] [apiKey] [prompt]
 */
public class Example06ReasoningStreaming
{
    public static void main(String[] args)
    {
        String model = args.length > 0 ? args[0] : "Qwen/Qwen3-4B";
        String apiKey = args.length > 1 ? args[1] : System.getenv("MODEL_SCOPE_API_KEY");
        String prompt = args.length > 2 ? args[2] : "请逐步思考并给出一个简短答案：为什么海水是咸的？";

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.exit(1);
        }

        AgentDefinition def = new AgentDefinition();
        def.setId("reasoning-agent");
        def.setName("Reasoning Agent");
        def.setModel(model);
        def.setSystemPrompt("You are a helpful assistant. Expose concise reasoning when provider supports reasoning streams.");
        def.setModelReasoning(Map.of("effort", "low"));

        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(def));

        AgentRunner runner = ExampleSupport.runnerWithPublisher(
            new ModelScopeLlmClient(apiKey, model),
            new ToolRegistry(),
            registry,
            null,
            createConsoleStreamingPublisher());

        RunResult result = runner.runStreamed(registry.get("reasoning-agent").orElseThrow(), prompt, RunConfiguration.defaults(), ExampleSupport.noopHooks());
        System.out.println("\nFinal output: " + result.getFinalOutput());
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
