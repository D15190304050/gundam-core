package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;
import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
/**
 * Class AbstractBuiltinTool.
 */

public abstract class AbstractBuiltinTool implements ITool
{
    /**
     * Field definition.
     */
    private final ToolDefinition definition;
    /**
     * Field category.
     */
    private final ToolCategory category;
    /**
     * Creates a new AbstractBuiltinTool instance.
     */

    protected AbstractBuiltinTool(ToolDefinition definition, ToolCategory category)
    {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.category = Objects.requireNonNull(category, "category");
    }

    /**
     * Executes definition.
     */
    @Override
    public ToolDefinition definition()
    {
        return definition;
    }
    /**
     * Executes category.
     */

    public ToolCategory category()
    {
        return category;
    }

    /**
     * Executes execute.
     */
    @Override
    public abstract String execute(Map<String, Object> input);
}
