package stark.dataworks.coderaider.gundam.core.runner;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsageTracker;
import stark.dataworks.coderaider.gundam.core.result.RunItem;

/**
 * RunnerContext implements end-to-end run orchestration including retries, guardrails, handoffs, and events.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
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
    private final List<RunItem> items = new ArrayList<>();

    /**
     * Internal state for memory; used while coordinating runtime behavior.
     */
    private final IAgentMemory memory;

    /**
     * Internal state for usage tracker used while coordinating runtime behavior.
     */
    private final TokenUsageTracker usageTracker = new TokenUsageTracker();

    /**
     * Internal state for current agent; used while coordinating runtime behavior.
     */
    @Setter
    private IAgent currentAgent;

    /**
     * Internal state for turns; used while coordinating runtime behavior.
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
