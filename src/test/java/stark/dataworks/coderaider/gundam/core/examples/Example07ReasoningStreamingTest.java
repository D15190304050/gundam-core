package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.event.RunEventType;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.streaming.IRunEventListener;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 7) How to stream reasoning and answer deltas separately.
 *
 * Usage: java Example07ReasoningStreaming [model] [apiKey] [prompt]
 */
public class Example07ReasoningStreamingTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        String prompt = "Think step by step and give a brief answer: why is seawater salty?";

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
        registry.register(def);

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(new ToolRegistry())
            .agentRegistry(registry)
            .eventPublisher(createConsoleStreamingPublisher())
            .build();

        ContextResult result = runner.chatClient("reasoning-agent").prompt().user(prompt).runConfiguration(RunConfiguration.defaults()).runHooks(ExampleSupport.noopHooks()).call().contextResult();
        System.out.println("\nFinal output: " + result.getFinalOutput());
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        return ExampleStreamingPublishers.reasoningAndText();
    }
}
