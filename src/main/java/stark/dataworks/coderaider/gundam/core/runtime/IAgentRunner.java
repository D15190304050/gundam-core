package stark.dataworks.coderaider.gundam.core.runtime;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;

/**
 * IAgentRunner implements single-step execution that binds model calls, tool calls, and memory updates.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface IAgentRunner
{

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param agent The agent used by this operation.
     * @param userInput The user input used by this operation.
     * @return The value produced by this operation.
     */
    AgentRunResult run(IAgent agent, String userInput);

    /**
     * Runs the primary execution flow with streamed model deltas.
     * @param agent The agent used by this operation.
     * @param userInput The user input used by this operation.
     * @return The value produced by this operation.
     */
    default AgentRunResult runStreamed(IAgent agent, String userInput)
    {
        return run(agent, userInput);
    }
}
