package stark.dataworks.coderaider.tool.builtin;

import java.util.Map;
import stark.dataworks.coderaider.tool.ToolCategory;
import stark.dataworks.coderaider.tool.ToolDefinition;

public class CodeInterpreterTool extends AbstractBuiltinTool {
    public CodeInterpreterTool(ToolDefinition definition) {
        super(definition, ToolCategory.CODE_INTERPRETER);
    }

    @Override
    public String execute(Map<String, Object> input) {
        return "CodeInterpreter(simulated): executed snippet length=" + String.valueOf(input.getOrDefault("code", "")).length();
    }
}
