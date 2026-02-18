package stark.dataworks.coderaider.gundam.core.tool;

import java.util.Optional;

/**
 * IToolRegistry implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface IToolRegistry
{

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param tool The tool used by this operation.
     */
    void register(ITool tool);

    /**
     * Returns the value requested by the caller from this IToolRegistry.
     * @param toolName The tool name used by this operation.
     * @return The value produced by this operation.
     */

    Optional<ITool> get(String toolName);
}
