package stark.dataworks.coderaider.agent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AgentRegistry implements IAgentRegistry {
    private final Map<String, IAgent> agents = new ConcurrentHashMap<>();

    @Override
    public void register(IAgent agent) {
        agents.put(agent.definition().getId(), agent);
    }

    @Override
    public Optional<IAgent> get(String agentId) {
        return Optional.ofNullable(agents.get(agentId));
    }
}
