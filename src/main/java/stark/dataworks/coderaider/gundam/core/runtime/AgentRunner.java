package stark.dataworks.coderaider.gundam.core.runtime;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.memory.InMemoryAgentMemory;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsageTracker;
/**
 * Class AgentRunner.
 */

public class AgentRunner implements IAgentRunner
{
    /**
     * Field stepEngine.
     */
    private final IStepEngine stepEngine;
    /**
     * Creates a new AgentRunner instance.
     */

    public AgentRunner(IStepEngine stepEngine)
    {
        this.stepEngine = stepEngine;
    }

    /**
     * Executes run.
     */
    @Override
    public AgentRunResult run(IAgent agent, String userInput)
    {
        IAgentMemory memory = new InMemoryAgentMemory();
        ExecutionContext context = new ExecutionContext(agent, memory, new TokenUsageTracker());
        return stepEngine.run(context, userInput);
    }
}
