package stark.dataworks.coderaider.gundam.core.runtime;

import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsageTracker;

/**
 * ExecutionContext implements single-step execution that binds model calls, tool calls, and memory updates.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class ExecutionContext
{

    /**
     * Internal state for agent; used while coordinating runtime behavior.
     */
    private IAgent agent;

    /**
     * Internal state for memory; used while coordinating runtime behavior.
     */
    private final IAgentMemory memory;

    /**
     * Internal state for token usage tracker; used while coordinating runtime behavior.
     */
    private final TokenUsageTracker tokenUsageTracker;

    /**
     * Internal state for current step; used while coordinating runtime behavior.
     */
    private int currentStep;

    /**
     * Performs execution context as part of ExecutionContext runtime responsibilities.
     * @param agent The agent used by this operation.
     * @param memory The memory used by this operation.
     * @param tokenUsageTracker The token usage tracker used by this operation.
     */
    public ExecutionContext(IAgent agent, IAgentMemory memory, TokenUsageTracker tokenUsageTracker)
    {
        this.agent = Objects.requireNonNull(agent, "agent");
        this.memory = Objects.requireNonNull(memory, "memory");
        this.tokenUsageTracker = Objects.requireNonNull(tokenUsageTracker, "tokenUsageTracker");
    }

    /**
     * Returns the current agent value maintained by this ExecutionContext.
     * @return The value produced by this operation.
     */
    public IAgent getAgent()
    {
        return agent;
    }

    /**
     * Updates the agent value used by this ExecutionContext for later operations.
     * @param agent The agent used by this operation.
     */
    public void setAgent(IAgent agent)
    {
        this.agent = agent;
    }

    /**
     * Returns the current memory value maintained by this ExecutionContext.
     * @return The value produced by this operation.
     */
    public IAgentMemory getMemory()
    {
        return memory;
    }

    /**
     * Returns the current token usage tracker value maintained by this ExecutionContext.
     * @return The value produced by this operation.
     */
    public TokenUsageTracker getTokenUsageTracker()
    {
        return tokenUsageTracker;
    }

    /**
     * Returns the current current step value maintained by this ExecutionContext.
     * @return The value produced by this operation.
     */
    public int getCurrentStep()
    {
        return currentStep;
    }

    /**
     * Performs increment step as part of ExecutionContext runtime responsibilities.
     */
    public void incrementStep()
    {
        currentStep++;
    }
}
