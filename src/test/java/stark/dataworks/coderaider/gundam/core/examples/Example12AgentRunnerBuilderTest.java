package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.event.RunEventType;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmClientRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.streaming.IRunEventListener;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 12) Demonstrates the AgentRunner builder API with streaming and reasoning support.
 */
public class Example12AgentRunnerBuilderTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        String prompt = "Why is the sky blue? Think step by step, and your answer must mention Rayleigh scattering.";

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

        ContextResult result = runner.chatClient("builder-agent")
            .prompt()
            .user(prompt)
            .runConfiguration(RunConfiguration.defaults())
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .contextResult();
        
        System.out.println("\n\n=== Final Output ===");
        System.out.println(result.getFinalOutput());
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        return ExampleStreamingPublishers.reasoningAndTextWithSections();
    }
}
