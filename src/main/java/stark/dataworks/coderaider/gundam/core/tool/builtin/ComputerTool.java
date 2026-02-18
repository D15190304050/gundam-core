package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * ComputerTool implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class ComputerTool extends AbstractBuiltinTool
{

    /**
     * Performs computer tool as part of ComputerTool runtime responsibilities.
     * @param definition The definition used by this operation.
     */
    public ComputerTool(ToolDefinition definition)
    {
        super(definition, ToolCategory.SHELL);
    }

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param input The input used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        String action = String.valueOf(input.getOrDefault("action", "noop"));
        return "ComputerTool(simulated): " + action;
    }
}
