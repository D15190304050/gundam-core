package stark.dataworks.coderaider.gundam.core.agent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory registry of runtime agents.
 * <p>
 * The runner and handoff router rely on this registry to resolve agent ids declared in JSON definitions into
 * executable {@link IAgent} instances.
 */
public class AgentRegistry implements IAgentRegistry
{

    /**
     * Thread-safe index of agents keyed by id for fast runtime lookup.
     */
    private final Map<String, IAgent> agents = new ConcurrentHashMap<>();

    /**
     * Registers an agent by its definition id.
     * @param agent Agent instance to make discoverable by id-based lookup.
     */
    @Override
    public void register(IAgent agent)
    {
        agents.put(agent.definition().getId(), agent);
    }

    /**
     * Looks up an agent by id.
     * @param agentId Agent definition id.
     * @return Matching agent if present; otherwise {@link Optional#empty()}.
     */
    @Override
    public Optional<IAgent> get(String agentId)
    {
        return Optional.ofNullable(agents.get(agentId));
    }
}
