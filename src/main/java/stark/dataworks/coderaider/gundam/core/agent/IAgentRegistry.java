package stark.dataworks.coderaider.gundam.core.agent;

import java.util.Optional;

/**
 * IAgentRegistry implements agent definitions and lookup used by runners and handoff resolution.
 */
public interface IAgentRegistry
{

    /**
     * Registers this value so it can be resolved in subsequent runtime operations.
     * @param agent agent instance.
     */
    void register(IAgent agent);

    /**
     * Returns the value requested by the caller from this IAgentRegistry.
     * @param agentId agent identifier.
     * @return Optional iagent value.
     */

    Optional<IAgent> get(String agentId);
}
