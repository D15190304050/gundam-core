package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;
import java.util.function.Function;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * FunctionTool implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class FunctionTool extends AbstractBuiltinTool
{

    /**
     * Internal state for function; used while coordinating runtime behavior.
     */
    private final Function<Map<String, Object>, String> function;

    /**
     * Performs function tool as part of FunctionTool runtime responsibilities.
     * @param definition The definition used by this operation.
     * @param function The function used by this operation.
     */
    public FunctionTool(ToolDefinition definition, Function<Map<String, Object>, String> function)
    {
        super(definition, ToolCategory.FUNCTION);
        this.function = function;
    }

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param input The input used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        return function.apply(input);
    }
}
