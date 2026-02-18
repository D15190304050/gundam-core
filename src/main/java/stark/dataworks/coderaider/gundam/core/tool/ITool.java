package stark.dataworks.coderaider.gundam.core.tool;

import java.util.Map;

/**
 * ITool implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface ITool
{

    /**
     * Performs definition as part of ITool runtime responsibilities.
     * @return The value produced by this operation.
     */
    ToolDefinition definition();

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param input The input used by this operation.
     * @return The value produced by this operation.
     */

    String execute(Map<String, Object> input);
}
