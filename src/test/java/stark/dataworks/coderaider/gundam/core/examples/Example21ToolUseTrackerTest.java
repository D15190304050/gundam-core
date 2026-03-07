package stark.dataworks.coderaider.gundam.core.examples;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tracking.AgentToolUseTracker;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Example demonstrating AgentToolUseTracker for tracking which tools each agent has used.
 * This supports model_settings resets based on tool usage history.
 */
public class Example21ToolUseTrackerTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));

        if (apiKey == null || apiKey.isBlank())
        {
            System.out.println("Skipping test: MODEL_SCOPE_API_KEY not set");
            return;
        }

        String model = "Qwen/Qwen3-4B";

        AgentDefinition agentDef = new AgentDefinition();
        agentDef.setId("tracker-agent");
        agentDef.setName("Tracker Agent");
        agentDef.setModel(model);
        agentDef.setSystemPrompt("You are a helpful assistant. Use tools when appropriate to answer questions.");
        agentDef.setToolNames(List.of("get_time", "get_weather"));

        IAgent agent = agentDef;

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(agent);

        AgentToolUseTracker tracker = new AgentToolUseTracker();

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(createGetTimeTool(tracker, agent));
        toolRegistry.register(createGetWeatherTool(tracker, agent));

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(ExampleStreamingPublishers.textWithToolLifecycle(""))
            .build();

        System.out.println("=== AgentToolUseTracker Example ===");
        System.out.println("Running agent with tool tracking...\n");

        ContextResult result = runner.chatClient("tracker-agent")
            .prompt()
            .user("What time is it and what's the weather in Tokyo?")
            .runConfiguration(RunConfiguration.defaults())
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .contextResult();

        System.out.println("\nFinal output: " + result.getFinalOutput());

        System.out.println("\n=== Tool Use Tracking Results ===");
        System.out.println("Has agent used tools? " + tracker.hasUsedTools(agent));
        System.out.println("Tools used by agent: " + tracker.getUsedTools(agent));

        System.out.println("\n=== Serialization Test ===");
        Map<String, List<String>> serialized = tracker.asSerializable();
        System.out.println("Serialized tracker state: " + serialized);

        AgentToolUseTracker restored = AgentToolUseTracker.fromSerializable(serialized);
        System.out.println("Restored tracker state: " + restored.asSerializable());
        System.out.println("Restored - has agent used tools? " + restored.hasUsedTools(agent));

        System.out.println("\nExample completed successfully!");
    }

    private static ITool createGetTimeTool(AgentToolUseTracker tracker, Agent agent)
    {
        return new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition(
                    "get_time",
                    "Get the current time",
                    List.of()
                );
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                tracker.recordUsedTools(agent, List.of("get_time"));
                return "{\"time\": \"12:00 PM\", \"timezone\": \"UTC\"}";
            }
        };
    }

    private static ITool createGetWeatherTool(AgentToolUseTracker tracker, Agent agent)
    {
        return new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition(
                    "get_weather",
                    "Get the current weather for a city",
                    List.of(
                        new ToolParameterSchema("city", "string", true, "The city name")
                    )
                );
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                tracker.recordUsedTools(agent, List.of("get_weather"));
                String city = (String) input.getOrDefault("city", "Unknown");
                return String.format("{\"city\": \"%s\", \"temperature\": 22, \"condition\": \"sunny\"}", city);
            }
        };
    }
}
