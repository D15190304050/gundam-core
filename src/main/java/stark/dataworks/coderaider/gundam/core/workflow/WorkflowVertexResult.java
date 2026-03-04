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
     * Internal state for outputs; used while coordinating runtime behavior.
     */
    private final Map<String, Object> outputs;

    /**
     * Internal state for next vertex ids; used while coordinating runtime behavior.
     */
    private final List<String> nextVertexIds;

    /**
     * Performs workflow vertex result as part of WorkflowVertexResult runtime responsibilities.
     * @param outputs The outputs used by this operation.
     * @param nextVertexIds The next vertex ids used by this operation.
     */
    public WorkflowVertexResult(Map<String, Object> outputs, List<String> nextVertexIds)
    {
        this.outputs = Collections.unmodifiableMap(Objects.requireNonNullElse(outputs, Map.of()));
        this.nextVertexIds = Collections.unmodifiableList(Objects.requireNonNullElse(nextVertexIds, List.of()));
    }

    /**
     * Performs of outputs as part of WorkflowVertexResult runtime responsibilities.
     * @param outputs The outputs used by this operation.
     * @return The value produced by this operation.
     */
    public static WorkflowVertexResult ofOutputs(Map<String, Object> outputs)
    {
        return new WorkflowVertexResult(outputs, List.of());
    }
}
