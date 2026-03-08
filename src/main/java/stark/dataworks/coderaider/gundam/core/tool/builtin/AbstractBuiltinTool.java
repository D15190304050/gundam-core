package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;
import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * AbstractBuiltinTool implements tool contracts, schema metadata, and executable tool registration.
 */
public abstract class AbstractBuiltinTool implements ITool
{

    /**
     * Immutable definition object that configures this runtime instance.
     */
    private final ToolDefinition definition;

    /**
     * Tool category used for routing and filtering.
     */
    private final ToolCategory category;

    /**
     * Initializes AbstractBuiltinTool with required runtime dependencies and options.
     *
     * @param definition definition object.
     * @param category   category.
     */
    protected AbstractBuiltinTool(ToolDefinition definition, ToolCategory category)
    {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.category = Objects.requireNonNull(category, "category");
    }

    /**
     * Returns definition metadata for this component.
     *
     * @return tool definition result.
     */
    @Override
    public ToolDefinition definition()
    {
        return definition;
    }

    /**
     * Returns the tool category used for routing.
     *
     * @return tool category result.
     */
    public ToolCategory category()
    {
        return category;
    }

    /**
     * Executes this tool operation and returns the produced output.
     *
     * @param input input payload.
     * @return abstract string result.
     */
    @Override
    public abstract String execute(Map<String, Object> input);
}
