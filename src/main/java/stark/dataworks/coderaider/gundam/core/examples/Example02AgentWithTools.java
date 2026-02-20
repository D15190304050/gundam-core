package stark.dataworks.coderaider.gundam.core.examples;

import java.util.List;
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
public class Example02AgentWithTools
{
    public static void main(String[] args)
    {
        String model = args.length > 0 ? args[0] : "Qwen/Qwen3-4B";
        String apiKey = args.length > 1 ? args[1] : System.getenv("MODEL_SCOPE_API_KEY");
        String city = args.length > 2 ? args[2] : "Shanghai";

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
        agentRegistry.register(new Agent(agentDef));

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(createWeatherLookupTool());
        toolRegistry.register(createUnitConvertTool());

        ModelScopeLlmClient llmClient = new ModelScopeLlmClient(apiKey, model);
        AgentRunner runner = ExampleSupport.runnerWithPublisher(llmClient, toolRegistry, agentRegistry, null, createConsoleStreamingPublisher());

        System.out.print("Streaming output: ");
        RunResult result = runner.runStreamed(agentRegistry.get("tool-agent").orElseThrow(), "What's the weather in " + city + "?", RunConfiguration.defaults(), ExampleSupport.noopHooks());
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
                else if (event.getType() == RunEventType.TOOL_CALL_REQUESTED)
                {
                    String tool = (String) event.getAttributes().get("tool");
                    System.out.println("\n[Tool call: " + tool + "]");
                }
                else if (event.getType() == RunEventType.TOOL_CALL_COMPLETED)
                {
                    String tool = (String) event.getAttributes().get("tool");
                    System.out.println("[Tool completed: " + tool + "]");
                    System.out.print("Continuing stream: ");
                }
            }
        });
        return publisher;
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
