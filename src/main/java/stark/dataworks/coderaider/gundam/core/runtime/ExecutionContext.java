package stark.dataworks.coderaider.gundam.core.runtime;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsageTracker;

/**
 * ExecutionContext implements single-step execution that binds model calls, tool calls, and memory updates.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
@Getter
public class ExecutionContext
{

    /**
     * Internal state for agent; used while coordinating runtime behavior.
     */
    @Setter
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
     * Performs increment step as part of ExecutionContext runtime responsibilities.
     */
    public void incrementStep()
    {
        currentStep++;
    }
}
