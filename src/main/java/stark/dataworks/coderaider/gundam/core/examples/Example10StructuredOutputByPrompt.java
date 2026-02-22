package stark.dataworks.coderaider.gundam.core.examples;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * Structured output by prompting the model with user-defined schema and JSON mode.
 */
public class Example10StructuredOutputByPrompt
{
    public static void main(String[] args)
    {
        String model = args.length > 0 ? args[0] : "Qwen/Qwen3-4B";
        String apiKey = args.length > 1 ? args[1] : System.getenv("MODEL_SCOPE_API_KEY");
        String topic = args.length > 2 ? args[2] : "Java";

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.err.println("Set MODEL_SCOPE_API_KEY environment variable or pass as second argument.");
            System.exit(1);
        }

        AgentDefinition definition = new AgentDefinition();
        definition.setId("structured-by-prompt");
        definition.setName("Structured By Prompt");
        definition.setModel(model);
        definition.setSystemPrompt("Always obey user schema exactly.");

        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(definition));

        String prompt = "Return only JSON with fields: topic(string), score(number), tags(array). Topic = " + topic + ".";
        RunConfiguration config = new RunConfiguration(8, null, 0.2, 512, "auto", "json_object", Map.of());
        RunResult result = ExampleSupport.runner(new ModelScopeLlmClient(apiKey, model), new ToolRegistry(), registry, null)
            .run(new Agent(definition), prompt, config, ExampleSupport.noopHooks());

        System.out.println("Prompt-defined JSON: " + result.getFinalOutput());
    }
}
