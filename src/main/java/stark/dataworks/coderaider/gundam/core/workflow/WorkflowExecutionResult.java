package stark.dataworks.coderaider.gundam.core.workflow;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * WorkflowExecutionResult implements workflow DAG run outputs.
 */
@Getter
public class WorkflowExecutionResult
{
    /**
     * Fallback or final output produced by error handling/execution.
     */
    private final String finalOutput;

    /**
     * Final workflow state snapshot at completion/failure.
     */
    private final Map<String, Object> finalState;

    /**
     * Completed vertex ids.
     */
    private final List<String> completedVertexIds;

    /**
     * Initializes WorkflowExecutionResult with required runtime dependencies and options.
     *
     * @param finalOutput        final output.
     * @param finalState         final state.
     * @param completedVertexIds completed vertex ids.
     */
    public WorkflowExecutionResult(String finalOutput, Map<String, Object> finalState, List<String> completedVertexIds)
    {
        this.finalOutput = Objects.requireNonNullElse(finalOutput, "");
        this.finalState = Collections.unmodifiableMap(Objects.requireNonNull(finalState, "finalState"));
        this.completedVertexIds = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(completedVertexIds, "completedVertexIds")));
    }
}
