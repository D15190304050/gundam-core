package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
/**
 * Class CodeInterpreterTool.
 */

public class CodeInterpreterTool extends AbstractBuiltinTool
{
    /**
     * Creates a new CodeInterpreterTool instance.
     */
    public CodeInterpreterTool(ToolDefinition definition)
    {
        super(definition, ToolCategory.CODE_INTERPRETER);
    }

    /**
     * Executes execute.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        return "CodeInterpreter(simulated): executed snippet length=" + String.valueOf(input.getOrDefault("code", "")).length();
    }
}
