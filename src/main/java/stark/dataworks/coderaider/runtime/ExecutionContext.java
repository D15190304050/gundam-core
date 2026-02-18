package stark.dataworks.coderaider.runtime;

import java.util.Objects;
import stark.dataworks.coderaider.agent.IAgent;
import stark.dataworks.coderaider.memory.IAgentMemory;
import stark.dataworks.coderaider.metrics.TokenUsageTracker;

public class ExecutionContext {
    private IAgent agent;
    private final IAgentMemory memory;
    private final TokenUsageTracker tokenUsageTracker;
    private int currentStep;

    public ExecutionContext(IAgent agent, IAgentMemory memory, TokenUsageTracker tokenUsageTracker) {
        this.agent = Objects.requireNonNull(agent, "agent");
        this.memory = Objects.requireNonNull(memory, "memory");
        this.tokenUsageTracker = Objects.requireNonNull(tokenUsageTracker, "tokenUsageTracker");
    }

    public IAgent getAgent() {
        return agent;
    }

    public void setAgent(IAgent agent) {
        this.agent = agent;
    }

    public IAgentMemory getMemory() {
        return memory;
    }

    public TokenUsageTracker getTokenUsageTracker() {
        return tokenUsageTracker;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void incrementStep() {
        currentStep++;
    }
}
