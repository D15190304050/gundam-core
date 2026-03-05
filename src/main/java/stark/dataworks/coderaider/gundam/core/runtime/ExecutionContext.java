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
     * Initializes ExecutionContext with required runtime dependencies and options.
     * @param agent agent instance.
     * @param memory conversation memory backend.
     * @param tokenUsageTracker token usage tracker.
     */
    public ExecutionContext(IAgent agent, IAgentMemory memory, TokenUsageTracker tokenUsageTracker)
    {
        this.agent = Objects.requireNonNull(agent, "agent");
        this.memory = Objects.requireNonNull(memory, "memory");
        this.tokenUsageTracker = Objects.requireNonNull(tokenUsageTracker, "tokenUsageTracker");
    }

    /**
     * Increments the current step counter.
     */
    public void incrementStep()
    {
        currentStep++;
    }
}
