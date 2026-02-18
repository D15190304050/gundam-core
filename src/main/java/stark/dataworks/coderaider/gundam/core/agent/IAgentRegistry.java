package stark.dataworks.coderaider.gundam.core.agent;

import java.util.Optional;

public interface IAgentRegistry {
    void register(IAgent agent);

    Optional<IAgent> get(String agentId);
}
