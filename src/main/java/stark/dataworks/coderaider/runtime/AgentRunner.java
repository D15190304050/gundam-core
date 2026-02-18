package stark.dataworks.coderaider.runtime;

import stark.dataworks.coderaider.agent.IAgent;
import stark.dataworks.coderaider.memory.IAgentMemory;
import stark.dataworks.coderaider.memory.InMemoryAgentMemory;
import stark.dataworks.coderaider.metrics.TokenUsageTracker;

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
