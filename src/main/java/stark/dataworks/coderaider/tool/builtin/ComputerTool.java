package stark.dataworks.coderaider.tool.builtin;

import java.util.Map;
import stark.dataworks.coderaider.tool.ToolCategory;
import stark.dataworks.coderaider.tool.ToolDefinition;

public class ComputerTool extends AbstractBuiltinTool {
    public ComputerTool(ToolDefinition definition) {
        super(definition, ToolCategory.SHELL);
    }

    @Override
    public String execute(Map<String, Object> input) {
        String action = String.valueOf(input.getOrDefault("action", "noop"));
        return "ComputerTool(simulated): " + action;
    }
}
