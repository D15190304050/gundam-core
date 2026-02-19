package stark.dataworks.coderaider.gundam.core.examples;

import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.model.Role;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 2) How to create an agent with a set of tools, and then run it.
 */
public class Example02AgentWithTools
{
    public static void main(String[] args)
    {
        String city = args.length > 0 ? args[0] : "Shanghai";

        AgentDefinition agentDef = new AgentDefinition();
        agentDef.setId("tool-agent");
        agentDef.setName("Tool Agent");
        agentDef.setModel("gpt-4.1-mini");
        agentDef.setSystemPrompt("Use tools to answer weather questions.");
        agentDef.setToolNames(List.of("weather_lookup", "unit_convert"));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(agentDef));

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(tool("weather_lookup", input -> "City=" + input.get("city") + ", TempC=26"));
        toolRegistry.register(tool("unit_convert", input -> "TempF=78.8"));

        AgentRunner runner = ExampleSupport.runner(new ToolCallingClient(city), toolRegistry, agentRegistry, null);
        RunResult result = runner.run(agentRegistry.get("tool-agent").orElseThrow(), "Weather in " + city + "?", RunConfiguration.defaults(), ExampleSupport.noopHooks());

        System.out.println("Output=" + result.getFinalOutput());
    }

    private static ITool tool(String name, java.util.function.Function<Map<String, Object>, String> fn)
    {
        return new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition(name, "example tool", List.of());
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                return fn.apply(input);
            }
        };
    }

    private static class ToolCallingClient implements ILlmClient
    {
        private final String city;

        private ToolCallingClient(String city)
        {
            this.city = city;
        }

        @Override
        public LlmResponse chat(LlmRequest request)
        {
            boolean hasToolOutputs = request.getMessages().stream().anyMatch(m -> m.getRole() == Role.TOOL);
            if (!hasToolOutputs)
            {
                return new LlmResponse("", List.of(
                    new ToolCall("weather_lookup", Map.of("city", city)),
                    new ToolCall("unit_convert", Map.of("celsius", 26))), null, new TokenUsage(14, 8));
            }
            return new LlmResponse("It is about 26°C (78.8°F) and clear.", List.of(), null, new TokenUsage(9, 17));
        }
    }
}
