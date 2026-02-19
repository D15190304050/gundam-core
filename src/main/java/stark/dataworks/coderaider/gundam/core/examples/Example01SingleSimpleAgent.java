package stark.dataworks.coderaider.gundam.core.examples;

import java.util.List;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 1) How to create & run a single simple agent.
 */
public class Example01SingleSimpleAgent
{
    public static void main(String[] args)
    {
        String model = args.length > 0 ? args[0] : "gpt-4.1-mini";
        String baseUrl = args.length > 1 ? args[1] : "https://<your-base-url>";
        String apiKey = args.length > 2 ? args[2] : "<your-api-key>";
        String prompt = args.length > 3 ? args[3] : "Introduce GUNDAM-core in one sentence.";

        AgentDefinition agentDef = new AgentDefinition();
        agentDef.setId("simple-agent");
        agentDef.setName("Simple Agent");
        agentDef.setModel(model);
        agentDef.setSystemPrompt("You are a concise assistant.");

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(agentDef));

        ILlmClient llmClient = new PlaceholderClient(baseUrl, apiKey);
        AgentRunner runner = ExampleSupport.runner(llmClient, new ToolRegistry(), agentRegistry, null);

        RunResult result = runner.run(agentRegistry.get("simple-agent").orElseThrow(), prompt, RunConfiguration.defaults(), ExampleSupport.noopHooks());
        System.out.println("Output=" + result.getFinalOutput());
    }

    private record PlaceholderClient(String baseUrl, String apiKey) implements ILlmClient
    {
        @Override
        public LlmResponse chat(LlmRequest request)
        {
            String user = request.getMessages().get(request.getMessages().size() - 1).getContent();
            return new LlmResponse("[placeholder] baseUrl=" + baseUrl + ", apiKey=****" + suffix(apiKey) + ", reply to: " + user,
                List.of(), null, new TokenUsage(12, 24));
        }

        private static String suffix(String key)
        {
            if (key == null || key.length() < 4)
            {
                return "0000";
            }
            return key.substring(key.length() - 4);
        }
    }
}
