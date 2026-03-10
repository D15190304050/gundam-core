package stark.dataworks.coderaider.genericagent.core.runner;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import stark.dataworks.coderaider.genericagent.core.agent.IAgent;
import stark.dataworks.coderaider.genericagent.core.context.ContextItem;
import stark.dataworks.coderaider.genericagent.core.events.RunEvent;
import stark.dataworks.coderaider.genericagent.core.memory.IAgentMemory;
import stark.dataworks.coderaider.genericagent.core.metrics.TokenUsageTracker;

/**
 * AgentRunnerContext implements end-to-end run orchestration including retries, guardrails, handoffs, and events.
 */
@Getter
public class AgentRunnerContext
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
     * Current step.
     */
    private int currentStep;

    /**
     * Number of turns already executed in the current run.
     */
    private int turns;

    /**
     * Initializes AgentRunnerContext with required runtime dependencies and options.
     *
     * @param currentAgent current agent.
     * @param memory       conversation memory backend.
     */
    public AgentRunnerContext(IAgent currentAgent, IAgentMemory memory)
    {
        this.currentAgent = Objects.requireNonNull(currentAgent, "currentAgent");
        this.memory = Objects.requireNonNull(memory, "memory");
    }

    /**
     * Increments the current step counter.
     */
    public void incrementStep()
    {
        currentStep++;
    }

    /**
     * Increments the executed turn counter.
     */
    public void incrementTurns()
    {
        this.turns++;
    }
}
