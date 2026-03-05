package stark.dataworks.coderaider.gundam.core.workflow;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * WorkflowVertexResult implements workflow DAG execution outputs.
 */
@Getter
public class WorkflowVertexResult
{
    /**
     * Vertex output map produced by this workflow step.
     */
    private final Map<String, Object> outputs;

    /**
     * Ordered list of next vertex ids.
     */
    private final List<String> nextVertexIds;

    /**
     * Initializes WorkflowVertexResult with required runtime dependencies and options.
     * @param Map<String map<string.
     * @param outputs outputs.
     * @param nextVertexIds next vertex ids.
     */
    public WorkflowVertexResult(Map<String, Object> outputs, List<String> nextVertexIds)
    {
        this.outputs = Collections.unmodifiableMap(Objects.requireNonNullElse(outputs, Map.of()));
        this.nextVertexIds = Collections.unmodifiableList(Objects.requireNonNullElse(nextVertexIds, List.of()));
    }

    /**
     * Creates a workflow vertex result using the processor output map.
     * @param Map<String map<string.
     * @param outputs outputs.
     * @return workflow vertex result.
     */
    public static WorkflowVertexResult ofOutputs(Map<String, Object> outputs)
    {
        return new WorkflowVertexResult(outputs, List.of());
    }
}
