package stark.dataworks.coderaider.gundam.core.runner;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsageTracker;
import stark.dataworks.coderaider.gundam.core.context.ContextItem;

/**
 * RunnerContext implements end-to-end run orchestration including retries, guardrails, handoffs, and events.
 */
@Getter
public class RunnerContext
{
    /**
     * Accumulated run events emitted during execution.
     */
    private final List<RunEvent> events = new ArrayList<>();

    /**
     * Timeline items describing user/model/tool actions.
     */
    private final List<ContextItem> items = new ArrayList<>();

    /**
     * Conversation memory for storing and retrieving prior messages.
     */
    private final IAgentMemory memory;

    /**
 * Accumulator that tracks token usage across the run.
     */
    private final TokenUsageTracker usageTracker = new TokenUsageTracker();

    /**
     * Agent currently responsible for handling the turn.
     */
    @Setter
    private IAgent currentAgent;

    /**
     * Number of turns already executed in the current run.
     */
    private int turns;

    /**
     * Performs runner context as part of RunnerContext runtime responsibilities.
     * @param currentAgent The current agent used by this operation.
     * @param memory The memory used by this operation.
     */
    public RunnerContext(IAgent currentAgent, IAgentMemory memory)
    {
        this.currentAgent = currentAgent;
        this.memory = memory;
    }

    /**
     * Performs increment turns as part of RunnerContext runtime responsibilities.
     */
    public void incrementTurns()
    {
        this.turns++;
    }
}
