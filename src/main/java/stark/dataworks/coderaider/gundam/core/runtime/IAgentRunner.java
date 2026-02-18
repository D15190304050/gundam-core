package stark.dataworks.coderaider.gundam.core.runtime;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;

public interface IAgentRunner {
    AgentRunResult run(IAgent agent, String userInput);
}
