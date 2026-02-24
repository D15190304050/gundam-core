package stark.dataworks.coderaider.gundam.core.examples;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.event.RunEventType;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmClientRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.streaming.IRunEventListener;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 12) Demonstrates the AgentRunner builder API with streaming and reasoning support.
 */
public class Example12AgentRunnerBuilder
{
    public static void main(String[] args)
    {
        String model = args.length > 0 ? args[0] : "Qwen/Qwen3-4B";
        String apiKey = args.length > 1 ? args[1] : System.getenv("MODEL_SCOPE_API_KEY");
        String prompt = args.length > 2 ? args[2] : "天空为什么是蓝色的？请逐步思考，回答必须包含瑞利散射。";

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
        definition.setSystemPrompt("You are a helpful assistant. Think step by step when solving problems.");
        definition.setModelReasoning(Map.of("effort", "low"));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(definition));

        AgentRunner runner = AgentRunner.builder()
            .llmClientRegistry(new LlmClientRegistry(Map.of("Qwen", new ModelScopeLlmClient(apiKey, model)), "Qwen"))
            .toolRegistry(new ToolRegistry())
            .agentRegistry(agentRegistry)
            .eventPublisher(createConsoleStreamingPublisher())
            .build();

        RunResult result = runner.runStreamed(
            new Agent(definition), 
            prompt, 
            RunConfiguration.defaults(), 
            ExampleSupport.noopHooks());
        
        System.out.println("\n\n=== Final Output ===");
        System.out.println(result.getFinalOutput());
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        RunEventPublisher publisher = new RunEventPublisher();
        publisher.subscribe(new IRunEventListener()
        {
            private boolean hasReasoning = false;
            private boolean hasAnswer = false;

            @Override
            public void onEvent(RunEvent event)
            {
                if (event.getType() == RunEventType.MODEL_REASONING_DELTA)
                {
                    String delta = (String) event.getAttributes().get("delta");
                    if (delta != null && !delta.isEmpty())
                    {
                        if (!hasReasoning)
                        {
                            System.out.println("=== Thinking ===");
                            hasReasoning = true;
                        }
                        System.out.print(delta);
                        System.out.flush();
                    }
                }
                else if (event.getType() == RunEventType.MODEL_RESPONSE_DELTA)
                {
                    String delta = (String) event.getAttributes().get("delta");
                    if (delta != null && !delta.isEmpty())
                    {
                        if (!hasAnswer && hasReasoning)
                        {
                            System.out.println("\n\n=== Answer ===");
                            hasAnswer = true;
                        }
                        System.out.print(delta);
                        System.out.flush();
                    }
                }
            }
        });
        return publisher;
    }
}
