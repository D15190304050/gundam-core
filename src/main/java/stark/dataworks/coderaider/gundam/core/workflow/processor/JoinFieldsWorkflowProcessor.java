package stark.dataworks.coderaider.gundam.core.workflow.processor;

import stark.dataworks.coderaider.gundam.core.workflow.IWorkflowVertexProcessor;
import stark.dataworks.coderaider.gundam.core.workflow.WorkflowVertexDefinition;
import stark.dataworks.coderaider.gundam.core.workflow.WorkflowVertexResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JoinFieldsWorkflowProcessor implements workflow DAG field-join behavior.
 */
public class JoinFieldsWorkflowProcessor implements IWorkflowVertexProcessor
{
    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param vertex The vertex used by this operation.
     * @param state The state used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public WorkflowVertexResult process(WorkflowVertexDefinition vertex, Map<String, Object> state)
    {
        Object fieldsObj = vertex.getConfig().getOrDefault("fields", List.of());
        @SuppressWarnings("unchecked")
        List<String> fields = fieldsObj instanceof List<?> list
            ? list.stream().map(String::valueOf).collect(Collectors.toList())
            : List.of();

        String separator = String.valueOf(vertex.getConfig().getOrDefault("separator", "\n"));
        String outputKey = String.valueOf(vertex.getConfig().getOrDefault("outputKey", "finalOutput"));

        String joined = fields.stream()
            .map(field -> String.valueOf(state.getOrDefault(field, "")))
            .filter(value -> !value.isBlank())
            .collect(Collectors.joining(separator));

        Map<String, Object> outputs = new HashMap<>();
        outputs.put(outputKey, joined);
        return WorkflowVertexResult.ofOutputs(outputs);
    }
}
