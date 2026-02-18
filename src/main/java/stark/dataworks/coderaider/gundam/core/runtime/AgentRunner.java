package stark.dataworks.coderaider.gundam.core.runtime;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.memory.InMemoryAgentMemory;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsageTracker;

/**
 * AgentRunner implements single-step execution that binds model calls, tool calls, and memory updates.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class AgentRunner implements IAgentRunner
{

    /**
     * Internal state for step engine; used while coordinating runtime behavior.
     */
    private final IStepEngine stepEngine;

    /**
     * Performs agent runner as part of AgentRunner runtime responsibilities.
     * @param stepEngine The step engine used by this operation.
     */
    public AgentRunner(IStepEngine stepEngine)
    {
        this.stepEngine = stepEngine;
    }

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param agent The agent used by this operation.
     * @param userInput The user input used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public AgentRunResult run(IAgent agent, String userInput)
    {
        IAgentMemory memory = new InMemoryAgentMemory();
        ExecutionContext context = new ExecutionContext(agent, memory, new TokenUsageTracker());
        return stepEngine.run(context, userInput);
    }
}
