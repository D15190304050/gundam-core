package stark.dataworks.coderaider.gundam.core.examples;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * Structured output by declaring Java type from developer side.
 */
public class Example09StructuredOutputByClass
{
    public static void main(String[] args)
    {
        String model = args.length > 0 ? args[0] : "Qwen/Qwen3-4B";
        String apiKey = args.length > 1 ? args[1] : System.getenv("MODEL_SCOPE_API_KEY");
        String prompt = args.length > 2 ? args[2] : "Generate a JSON summary of a sprint plan.";

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.err.println("Set MODEL_SCOPE_API_KEY environment variable or pass as second argument.");
            System.exit(1);
        }

        AgentDefinition definition = new AgentDefinition();
        definition.setId("structured-by-class");
        definition.setName("Structured By Class");
        definition.setModel(model);
        definition.setSystemPrompt("Return concise structured summaries.");

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(definition));

        AgentRunner runner = ExampleSupport.runner(new ModelScopeLlmClient(apiKey, model), new ToolRegistry(), agentRegistry, null);
        RunResult result = runner.run(new Agent(definition), prompt, RunConfiguration.defaults(), ExampleSupport.noopHooks(), SprintSummary.class);

        System.out.println("Structured output text: " + result.getFinalOutput());
        if (!result.getItems().isEmpty())
        {
            Object payload = result.getItems().get(result.getItems().size() - 1).getMetadata();
            System.out.println("Structured output payload: " + payload);
        }
    }

    private static final class SprintSummary
    {
        private String title;
        private int priority;
        private boolean blocked;
    }
}
