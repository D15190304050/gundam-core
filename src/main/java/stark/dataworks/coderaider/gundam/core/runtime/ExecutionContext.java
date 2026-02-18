package stark.dataworks.coderaider.gundam.core.runtime;

import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsageTracker;
/**
 * Class ExecutionContext.
 */

public class ExecutionContext
{
    /**
     * Field agent.
     */
    private IAgent agent;
    /**
     * Field memory.
     */
    private final IAgentMemory memory;
    /**
     * Field tokenUsageTracker.
     */
    private final TokenUsageTracker tokenUsageTracker;
    /**
     * Field currentStep.
     */
    private int currentStep;
    /**
     * Creates a new ExecutionContext instance.
     */

    public ExecutionContext(IAgent agent, IAgentMemory memory, TokenUsageTracker tokenUsageTracker)
    {
        this.agent = Objects.requireNonNull(agent, "agent");
        this.memory = Objects.requireNonNull(memory, "memory");
        this.tokenUsageTracker = Objects.requireNonNull(tokenUsageTracker, "tokenUsageTracker");
    }
    /**
     * Executes getAgent.
     */

    public IAgent getAgent()
    {
        return agent;
    }
    /**
     * Executes setAgent.
     */

    public void setAgent(IAgent agent)
    {
        this.agent = agent;
    }
    /**
     * Executes getMemory.
     */

    public IAgentMemory getMemory()
    {
        return memory;
    }
    /**
     * Executes getTokenUsageTracker.
     */

    public TokenUsageTracker getTokenUsageTracker()
    {
        return tokenUsageTracker;
    }
    /**
     * Executes getCurrentStep.
     */

    public int getCurrentStep()
    {
        return currentStep;
    }
    /**
     * Executes incrementStep.
     */

    public void incrementStep()
    {
        currentStep++;
    }
}
