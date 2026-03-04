package stark.dataworks.coderaider.gundam.core.tool.builtin;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.workflow.WorkflowExecutionResult;
import stark.dataworks.coderaider.gundam.core.workflow.WorkflowExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * WorkflowTool implements tool contracts, schema metadata, and executable tool registration.
 */
public class WorkflowTool extends AbstractBuiltinTool
{
    /**
     * Executor used to run workflow DAGs as a tool.
     */
    private final WorkflowExecutor workflowExecutor;

    /**
     * Performs workflow tool as part of WorkflowTool runtime responsibilities.
     * @param definition The definition used by this operation.
     * @param workflowExecutor The workflow executor used by this operation.
     */
    public WorkflowTool(ToolDefinition definition, WorkflowExecutor workflowExecutor)
    {
        super(definition, ToolCategory.FUNCTION);
        this.workflowExecutor = workflowExecutor;
    }

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param input The input used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        WorkflowExecutionResult result = workflowExecutor.execute(new HashMap<>(input));
        return result.getFinalOutput();
    }
}
