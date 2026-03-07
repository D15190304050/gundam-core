package stark.dataworks.coderaider.gundam.core.examples;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.util.List;
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
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 2) How to create an agent with a set of tools, and then run it with streaming output.
 * 
 * Usage: java Example02AgentWithTools [model] [apiKey] [city]
 * - model: ModelScope model name (default: Qwen/Qwen3-4B)
 * - apiKey: Your ModelScope API key (required, or set MODEL_SCOPE_API_KEY env var)
 * - city: City name for weather lookup (default: Shanghai)
 */
public class Example02AgentWithToolsTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        String city = "Shanghai";

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.err.println("Set MODEL_SCOPE_API_KEY environment variable or pass as second argument.");
            System.exit(1);
        }

        AgentDefinition agentDef = new AgentDefinition();
        agentDef.setId("tool-agent");
        agentDef.setName("Tool Agent");
        agentDef.setModel(model);
        agentDef.setSystemPrompt("Use tools to answer weather questions. When asked about weather, first use weather_lookup to get the temperature, then use unit_convert to provide both Celsius and Fahrenheit.");
        agentDef.setToolNames(List.of("weather_lookup", "unit_convert"));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(agentDef);

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(createWeatherLookupTool());
        toolRegistry.register(createUnitConvertTool());

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(createConsoleStreamingPublisher())
            .build();

        System.out.print("Streaming output: ");
        ContextResult result = runner.chatClient("tool-agent").prompt().user("What's the weather in " + city + "?").runConfiguration(RunConfiguration.defaults()).runHooks(ExampleSupport.noopHooks()).call().contextResult();
        System.out.println();
        System.out.println("Final output: " + result.getFinalOutput());
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        return ExampleStreamingPublishers.textWithToolLifecycle("");
    }

    private static ITool createWeatherLookupTool()
    {
        return new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition(
                    "weather_lookup",
                    "Look up current weather information for a city",
                    List.of(
                        new ToolParameterSchema("city", "string", true, "The city name to look up weather for")
                    ));
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                String city = (String) input.getOrDefault("city", "Unknown");
                return String.format("{\"city\": \"%s\", \"temperature_celsius\": 26, \"condition\": \"clear\", \"humidity\": 65}", city);
            }
        };
    }

    private static ITool createUnitConvertTool()
    {
        return new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition(
                    "unit_convert",
                    "Convert temperature from Celsius to Fahrenheit",
                    List.of(
                        new ToolParameterSchema("celsius", "number", true, "Temperature in Celsius to convert")
                    ));
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                double celsius = ((Number) input.getOrDefault("celsius", 0)).doubleValue();
                double fahrenheit = celsius * 9 / 5 + 32;
                return String.format("{\"celsius\": %.1f, \"fahrenheit\": %.1f}", celsius, fahrenheit);
            }
        };
    }
}
