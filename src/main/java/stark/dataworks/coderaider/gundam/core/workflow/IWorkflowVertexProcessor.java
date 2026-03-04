package stark.dataworks.coderaider.gundam.core.workflow;

import java.util.Map;

/**
 * IWorkflowVertexProcessor implements workflow DAG vertex execution contracts.
 */
public interface IWorkflowVertexProcessor
{
    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param vertex The vertex used by this operation.
     * @param state The state used by this operation.
     * @return The value produced by this operation.
     */
    WorkflowVertexResult process(WorkflowVertexDefinition vertex, Map<String, Object> state);
}
