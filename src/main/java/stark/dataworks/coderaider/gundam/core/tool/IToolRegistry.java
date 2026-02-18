package stark.dataworks.coderaider.gundam.core.tool;

import java.util.Optional;
/**
 * Interface IToolRegistry.
 */

public interface IToolRegistry
{
    /**
     * Executes register.
     */
    void register(ITool tool);
    /**
     * Executes get.
     */

    Optional<ITool> get(String toolName);
}
