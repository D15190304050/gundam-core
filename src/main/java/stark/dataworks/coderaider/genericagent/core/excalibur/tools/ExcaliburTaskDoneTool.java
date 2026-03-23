package stark.dataworks.coderaider.genericagent.core.excalibur.tools;

import stark.dataworks.coderaider.genericagent.core.tool.ToolCategory;
import stark.dataworks.coderaider.genericagent.core.tool.ToolDefinition;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.AbstractBuiltinTool;

import java.util.List;
import java.util.Map;

/**
 * Trae-compatible task completion marker tool.
 */
public final class ExcaliburTaskDoneTool extends AbstractBuiltinTool
{
    public ExcaliburTaskDoneTool()
    {
        super(new ToolDefinition("task_done", "Signal that the task is complete.", List.of()), ToolCategory.FUNCTION);
    }

    @Override
    public String execute(Map<String, Object> input)
    {
        return "Task done.";
    }
}
