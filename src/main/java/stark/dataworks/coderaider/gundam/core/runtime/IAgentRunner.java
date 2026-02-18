package stark.dataworks.coderaider.gundam.core.runtime;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
/**
 * Interface IAgentRunner.
 */

public interface IAgentRunner
{
    /**
     * Executes run.
     */
    AgentRunResult run(IAgent agent, String userInput);
}
