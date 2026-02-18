package stark.dataworks.coderaider.tool.builtin;

import java.util.Map;
import java.util.function.Function;
import stark.dataworks.coderaider.tool.ToolCategory;
import stark.dataworks.coderaider.tool.ToolDefinition;

public class FunctionTool extends AbstractBuiltinTool {
    private final Function<Map<String, Object>, String> function;

    public FunctionTool(ToolDefinition definition, Function<Map<String, Object>, String> function) {
        super(definition, ToolCategory.FUNCTION);
        this.function = function;
    }

    @Override
    public String execute(Map<String, Object> input) {
        return function.apply(input);
    }
}
