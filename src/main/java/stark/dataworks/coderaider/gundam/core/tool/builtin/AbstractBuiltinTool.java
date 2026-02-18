package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;
import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * AbstractBuiltinTool implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public abstract class AbstractBuiltinTool implements ITool
{

    /**
     * Internal state for definition; used while coordinating runtime behavior.
     */
    private final ToolDefinition definition;

    /**
     * Internal state for category; used while coordinating runtime behavior.
     */
    private final ToolCategory category;

    /**
     * Performs abstract builtin tool as part of AbstractBuiltinTool runtime responsibilities.
     * @param definition The definition used by this operation.
     * @param category The category used by this operation.
     */
    protected AbstractBuiltinTool(ToolDefinition definition, ToolCategory category)
    {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.category = Objects.requireNonNull(category, "category");
    }

    /**
     * Performs definition as part of AbstractBuiltinTool runtime responsibilities.
     * @return The value produced by this operation.
     */
    @Override
    public ToolDefinition definition()
    {
        return definition;
    }

    /**
     * Performs category as part of AbstractBuiltinTool runtime responsibilities.
     * @return The value produced by this operation.
     */
    public ToolCategory category()
    {
        return category;
    }

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param input The input used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public abstract String execute(Map<String, Object> input);
}
