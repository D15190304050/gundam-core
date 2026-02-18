package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;
import java.util.function.Function;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
/**
 * Class FunctionTool.
 */

public class FunctionTool extends AbstractBuiltinTool
{
    /**
     * Field function.
     */
    private final Function<Map<String, Object>, String> function;
    /**
     * Creates a new FunctionTool instance.
     */

    public FunctionTool(ToolDefinition definition, Function<Map<String, Object>, String> function)
    {
        super(definition, ToolCategory.FUNCTION);
        this.function = function;
    }

    /**
     * Executes execute.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        return function.apply(input);
    }
}
