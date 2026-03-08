package stark.dataworks.coderaider.gundam.core.workflow.processor;

import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.IRunHooks;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.workflow.IWorkflowVertexProcessor;
import stark.dataworks.coderaider.gundam.core.workflow.WorkflowVertexDefinition;
import stark.dataworks.coderaider.gundam.core.workflow.WorkflowVertexResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * AgentWorkflowProcessor implements workflow DAG vertex execution by delegating a step to an agent.
 */
public class AgentWorkflowProcessor implements IWorkflowVertexProcessor
{
    /**
     * Agent runner used to execute nested agent calls.
     */
    private final AgentRunner runner;

    /**
     * Run configuration applied to delegated agent execution.
     */
    private final RunConfiguration runConfiguration;

    /**
     * Initializes AgentWorkflowProcessor with required runtime dependencies and options.
     *
     * @param runner           agent runner.
     * @param runConfiguration run configuration.
     */
    public AgentWorkflowProcessor(AgentRunner runner, RunConfiguration runConfiguration)
    {
        this.runner = Objects.requireNonNull(runner, "runner");
        this.runConfiguration = runConfiguration == null ? RunConfiguration.defaults() : runConfiguration;
    }

    /**
     * Processes the supplied workflow/tracing input.
     *
     * @param vertex vertex.
     * @param state  state.
     * @return workflow vertex result.
     */
    @Override
    public WorkflowVertexResult process(WorkflowVertexDefinition vertex, Map<String, Object> state)
    {
        String agentId = String.valueOf(vertex.getConfig().getOrDefault("agentId", ""));
        if (agentId.isBlank())
        {
            throw new IllegalArgumentException("agentId is required for agent processor");
        }

        String outputKey = String.valueOf(vertex.getConfig().getOrDefault("outputKey", vertex.getId() + "Output"));
        String promptTemplate = String.valueOf(vertex.getConfig().getOrDefault("promptTemplate", "{{input}}"));
        String renderedPrompt = renderTemplate(promptTemplate, state);

        String output = runner.chatClient(agentId)
            .prompt()
            .user(renderedPrompt)
            .runConfiguration(runConfiguration)
            .runHooks(new IRunHooks()
            {
            })
            .call()
            .content();

        Map<String, Object> outputs = new HashMap<>();
        outputs.put(outputKey, output);
        return WorkflowVertexResult.ofOutputs(outputs);
    }

    private String renderTemplate(String template, Map<String, Object> state)
    {
        String rendered = template;
        for (Map.Entry<String, Object> entry : state.entrySet())
        {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return rendered;
    }
}
