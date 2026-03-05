package stark.dataworks.coderaider.gundam.core.workflow.processor;

import stark.dataworks.coderaider.gundam.core.workflow.IWorkflowVertexProcessor;
import stark.dataworks.coderaider.gundam.core.workflow.WorkflowVertexDefinition;
import stark.dataworks.coderaider.gundam.core.workflow.WorkflowVertexResult;

import java.util.HashMap;
import java.util.Map;

/**
 * TemplateRenderWorkflowProcessor implements workflow DAG template rendering.
 */
public class TemplateRenderWorkflowProcessor implements IWorkflowVertexProcessor
{
    /**
     * Processes the supplied workflow/tracing input.
     * @param vertex vertex.
     * @param Map<String map<string.
     * @param state state.
     * @return workflow vertex result.
     */
    @Override
    public WorkflowVertexResult process(WorkflowVertexDefinition vertex, Map<String, Object> state)
    {
        String template = String.valueOf(vertex.getConfig().getOrDefault("template", ""));
        String outputKey = String.valueOf(vertex.getConfig().getOrDefault("outputKey", vertex.getId() + "Output"));

        String rendered = template;
        for (Map.Entry<String, Object> entry : state.entrySet())
        {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }

        Map<String, Object> outputs = new HashMap<>();
        outputs.put(outputKey, rendered);
        return WorkflowVertexResult.ofOutputs(outputs);
    }
}
