package stark.dataworks.coderaider.gundam.core.agent;

import java.util.Optional;
/**
 * Interface IAgentRegistry.
 */

public interface IAgentRegistry
{
    /**
     * Executes register.
     */
    void register(IAgent agent);
    /**
     * Executes get.
     */

    Optional<IAgent> get(String agentId);
}
