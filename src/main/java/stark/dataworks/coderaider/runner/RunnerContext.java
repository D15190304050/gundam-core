package stark.dataworks.coderaider.runner;

import java.util.ArrayList;
import java.util.List;
import stark.dataworks.coderaider.agent.IAgent;
import stark.dataworks.coderaider.event.RunEvent;
import stark.dataworks.coderaider.memory.IAgentMemory;
import stark.dataworks.coderaider.metrics.TokenUsageTracker;
import stark.dataworks.coderaider.result.RunItem;

public class RunnerContext {
    private final List<RunEvent> events = new ArrayList<>();
    private final List<RunItem> items = new ArrayList<>();
    private final IAgentMemory memory;
    private final TokenUsageTracker usageTracker = new TokenUsageTracker();
    private IAgent currentAgent;
    private int turns;

    public RunnerContext(IAgent currentAgent, IAgentMemory memory) {
        this.currentAgent = currentAgent;
        this.memory = memory;
    }

    public List<RunEvent> getEvents() {
        return events;
    }

    public List<RunItem> getItems() {
        return items;
    }

    public IAgentMemory getMemory() {
        return memory;
    }

    public TokenUsageTracker getUsageTracker() {
        return usageTracker;
    }

    public IAgent getCurrentAgent() {
        return currentAgent;
    }

    public void setCurrentAgent(IAgent currentAgent) {
        this.currentAgent = currentAgent;
    }

    public int getTurns() {
        return turns;
    }

    public void incrementTurns() {
        this.turns++;
    }
}
