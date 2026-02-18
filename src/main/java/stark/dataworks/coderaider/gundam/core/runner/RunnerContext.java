package stark.dataworks.coderaider.gundam.core.runner;

import java.util.ArrayList;
import java.util.List;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsageTracker;
import stark.dataworks.coderaider.gundam.core.result.RunItem;
/**
 * Class RunnerContext.
 */

public class RunnerContext
{
    /**
     * Field events.
     */
    private final List<RunEvent> events = new ArrayList<>();
    /**
     * Field items.
     */
    private final List<RunItem> items = new ArrayList<>();
    /**
     * Field memory.
     */
    private final IAgentMemory memory;
    /**
     * Field usageTracker.
     */
    private final TokenUsageTracker usageTracker = new TokenUsageTracker();
    /**
     * Field currentAgent.
     */
    private IAgent currentAgent;
    /**
     * Field turns.
     */
    private int turns;
    /**
     * Creates a new RunnerContext instance.
     */

    public RunnerContext(IAgent currentAgent, IAgentMemory memory)
    {
        this.currentAgent = currentAgent;
        this.memory = memory;
    }
    /**
     * Executes getEvents.
     */

    public List<RunEvent> getEvents()
    {
        return events;
    }
    /**
     * Executes getItems.
     */

    public List<RunItem> getItems()
    {
        return items;
    }
    /**
     * Executes getMemory.
     */

    public IAgentMemory getMemory()
    {
        return memory;
    }
    /**
     * Executes getUsageTracker.
     */

    public TokenUsageTracker getUsageTracker()
    {
        return usageTracker;
    }
    /**
     * Executes getCurrentAgent.
     */

    public IAgent getCurrentAgent()
    {
        return currentAgent;
    }
    /**
     * Executes setCurrentAgent.
     */

    public void setCurrentAgent(IAgent currentAgent)
    {
        this.currentAgent = currentAgent;
    }
    /**
     * Executes getTurns.
     */

    public int getTurns()
    {
        return turns;
    }
    /**
     * Executes incrementTurns.
     */

    public void incrementTurns()
    {
        this.turns++;
    }
}
