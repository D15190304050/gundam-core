package stark.dataworks.coderaider.agent;

import java.util.Optional;

public interface IAgentRegistry {
    void register(IAgent agent);

    Optional<IAgent> get(String agentId);
}
