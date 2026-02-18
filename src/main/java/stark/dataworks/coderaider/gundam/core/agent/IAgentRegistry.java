package stark.dataworks.coderaider.gundam.core.agent;

import java.util.Optional;

/**
 * IAgentRegistry implements agent definitions and lookup used by runners and handoff resolution.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface IAgentRegistry
{

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param agent The agent used by this operation.
     */
    void register(IAgent agent);

    /**
     * Returns the value requested by the caller from this IAgentRegistry.
     * @param agentId The agent id used by this operation.
     * @return The value produced by this operation.
     */

    Optional<IAgent> get(String agentId);
}
