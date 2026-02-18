package stark.dataworks.coderaider.tool.builtin;

import java.util.Map;
import java.util.Objects;
import stark.dataworks.coderaider.tool.ITool;
import stark.dataworks.coderaider.tool.ToolCategory;
import stark.dataworks.coderaider.tool.ToolDefinition;

public abstract class AbstractBuiltinTool implements ITool {
    private final ToolDefinition definition;
    private final ToolCategory category;

    protected AbstractBuiltinTool(ToolDefinition definition, ToolCategory category) {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.category = Objects.requireNonNull(category, "category");
    }

    @Override
    public ToolDefinition definition() {
        return definition;
    }

    public ToolCategory category() {
        return category;
    }

    @Override
    public abstract String execute(Map<String, Object> input);
}
