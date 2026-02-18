package stark.dataworks.coderaider.gundam.core.runtime;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.memory.InMemoryAgentMemory;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsageTracker;

public class AgentRunner implements IAgentRunner {
    private final IStepEngine stepEngine;

    public AgentRunner(IStepEngine stepEngine) {
        this.stepEngine = stepEngine;
    }

    @Override
    public AgentRunResult run(IAgent agent, String userInput) {
        IAgentMemory memory = new InMemoryAgentMemory();
        ExecutionContext context = new ExecutionContext(agent, memory, new TokenUsageTracker());
        return stepEngine.run(context, userInput);
    }
}
