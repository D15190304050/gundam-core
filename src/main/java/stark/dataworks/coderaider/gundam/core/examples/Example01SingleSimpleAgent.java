package stark.dataworks.coderaider.gundam.core.examples;

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
 * 1) How to create & run a single simple agent with streaming output.
 * 
 * Usage: java Example01SingleSimpleAgent [model] [apiKey] [prompt]
 * - model: ModelScope model name (default: Qwen/Qwen3-4B)
 * - apiKey: Your ModelScope API key (required, or set MODEL_SCOPE_API_KEY env var)
 * - prompt: User prompt (default: "Introduce GUNDAM-core in one sentence.")
 */
public class Example01SingleSimpleAgent
{
    public static void main(String[] args)
    {
        String model = args.length > 0 ? args[0] : "Qwen/Qwen3-4B";
        String apiKey = args.length > 1 ? args[1] : System.getenv("MODEL_SCOPE_API_KEY");
        String prompt = args.length > 2 ? args[2] : "如何制作红烧牛肉面？我需要详细的说明。";

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
        agentRegistry.register(new Agent(agentDef));

        ModelScopeLlmClient llmClient = new ModelScopeLlmClient(apiKey, model);
        AgentRunner runner = ExampleSupport.runnerWithPublisher(llmClient, new ToolRegistry(), agentRegistry, null, createConsoleStreamingPublisher());

        System.out.print("Streaming output: ");
        RunResult result = runner.runStreamed(agentRegistry.get("simple-agent").orElseThrow(), prompt, RunConfiguration.defaults(), ExampleSupport.noopHooks());
        System.out.println();
        System.out.println("Final output: " + result.getFinalOutput());
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        RunEventPublisher publisher = new RunEventPublisher();
        publisher.subscribe(new IRunEventListener()
        {
            @Override
            public void onEvent(RunEvent event)
            {
                if (event.getType() == RunEventType.MODEL_RESPONSE_DELTA)
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
