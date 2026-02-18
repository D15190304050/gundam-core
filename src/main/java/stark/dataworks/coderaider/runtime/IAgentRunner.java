package stark.dataworks.coderaider.runtime;

import stark.dataworks.coderaider.agent.IAgent;

public interface IAgentRunner {
    AgentRunResult run(IAgent agent, String userInput);
}
