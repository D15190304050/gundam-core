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
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 7) How to attach Anthropic-style agent skills and stream output.
 *
 * Usage: java Example07AgentWithSkillsStreaming [model] [apiKey] [prompt] [skillId]
 */
public class Example07AgentWithSkillsStreaming
{
    public static void main(String[] args)
    {
        String model = args.length > 0 ? args[0] : "Qwen/Qwen3-4B";
        String apiKey = args.length > 1 ? args[1] : System.getenv("MODEL_SCOPE_API_KEY");
        String prompt = args.length > 2 ? args[2] : "Give me a short checklist to review an unfamiliar Java repository.";
        String skillId = args.length > 3 ? args[3] : System.getenv("ANTHROPIC_SKILL_ID");

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.exit(1);
        }

        AgentDefinition def = new AgentDefinition();
        def.setId("skills-agent");
        def.setName("Skills Agent");
        def.setModel(model);
        def.setSystemPrompt("You are a practical engineering assistant.");

        if (skillId != null && !skillId.isBlank())
        {
            def.setModelSkills(List.of(Map.of(
                "type", "skill_reference",
                "skill_id", skillId)));
            System.out.println("Using Anthropic skill reference: " + skillId);
        }
        else
        {
            System.out.println("No ANTHROPIC_SKILL_ID provided. Running without remote skill payload.");
        }

        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(def));

        AgentRunner runner = ExampleSupport.runnerWithPublisher(
            new ModelScopeLlmClient(apiKey, model),
            new ToolRegistry(),
            registry,
            null,
            createConsoleStreamingPublisher());

        RunResult result = runner.runStreamed(registry.get("skills-agent").orElseThrow(), prompt, RunConfiguration.defaults(), ExampleSupport.noopHooks());
        System.out.println("\nFinal output: " + result.getFinalOutput());
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
            }
        });
        return publisher;
    }
}
