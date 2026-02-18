package stark.dataworks.coderaider.gundam.core.runner;

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
     * Returns the current events value maintained by this RunnerContext.
     * @return The value produced by this operation.
     */
    public List<RunEvent> getEvents()
    {
        return events;
    }

    /**
     * Returns the current items value maintained by this RunnerContext.
     * @return The value produced by this operation.
     */
    public List<RunItem> getItems()
    {
        return items;
    }

    /**
     * Returns the current memory value maintained by this RunnerContext.
     * @return The value produced by this operation.
     */
    public IAgentMemory getMemory()
    {
        return memory;
    }

    /**
     * Returns the current usage tracker value maintained by this RunnerContext.
     * @return The value produced by this operation.
     */
    public TokenUsageTracker getUsageTracker()
    {
        return usageTracker;
    }

    /**
     * Returns the current current agent value maintained by this RunnerContext.
     * @return The value produced by this operation.
     */
    public IAgent getCurrentAgent()
    {
        return currentAgent;
    }

    /**
     * Updates the current agent value used by this RunnerContext for later operations.
     * @param currentAgent The current agent used by this operation.
     */
    public void setCurrentAgent(IAgent currentAgent)
    {
        this.currentAgent = currentAgent;
    }

    /**
     * Returns the current turns value maintained by this RunnerContext.
     * @return The value produced by this operation.
     */
    public int getTurns()
    {
        return turns;
    }

    /**
     * Performs increment turns as part of RunnerContext runtime responsibilities.
     */
    public void incrementTurns()
    {
        this.turns++;
    }
}
