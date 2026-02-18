package stark.dataworks.coderaider.gundam.core.agent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Class AgentRegistry.
 */

public class AgentRegistry implements IAgentRegistry
{
    /**
     * Field agents.
     */
    private final Map<String, IAgent> agents = new ConcurrentHashMap<>();

    /**
     * Executes register.
     */
    @Override
    public void register(IAgent agent)
    {
        agents.put(agent.definition().getId(), agent);
    }

    /**
     * Executes get.
     */
    @Override
    public Optional<IAgent> get(String agentId)
    {
        return Optional.ofNullable(agents.get(agentId));
    }
}
