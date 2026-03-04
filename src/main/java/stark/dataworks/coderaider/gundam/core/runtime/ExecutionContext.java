package stark.dataworks.coderaider.gundam.core.runtime;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsageTracker;

/**
 * ExecutionContext implements single-step execution that binds model calls, tool calls, and memory updates.
 */
@Getter
public class ExecutionContext
{

    /**
     * Current agent bound to this execution context.
     */
    @Setter
    private IAgent agent;

    /**
     * Conversation memory for storing and retrieving prior messages.
     */
    private final IAgentMemory memory;

    /**
     * Token accounting data used for cost and quota tracking.
     */
    private final TokenUsageTracker tokenUsageTracker;

    /**
     * Current step.
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
