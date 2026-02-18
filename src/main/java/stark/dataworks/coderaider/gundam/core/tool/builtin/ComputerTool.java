package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
/**
 * Class ComputerTool.
 */

public class ComputerTool extends AbstractBuiltinTool
{
    /**
     * Creates a new ComputerTool instance.
     */
    public ComputerTool(ToolDefinition definition)
    {
        super(definition, ToolCategory.SHELL);
    }

    /**
     * Executes execute.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        String action = String.valueOf(input.getOrDefault("action", "noop"));
        return "ComputerTool(simulated): " + action;
    }
}
