package stark.dataworks.coderaider.gundam.core.runtime;

import lombok.Getter;

import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;

/**
 * AgentRunResult implements single-step execution that binds model calls, tool calls, and memory updates.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
@Getter
public class AgentRunResult
{

    /**
     * Internal state for output; used while coordinating runtime behavior.
     */
    private final String output;

    /**
     * Internal state for usage; used while coordinating runtime behavior.
     */
    private final TokenUsage usage;

    /**
     * Internal state for final agent id; used while coordinating runtime behavior.
     */
    private final String finalAgentId;

    /**
     * Performs agent run result as part of AgentRunResult runtime responsibilities.
     * @param output The output used by this operation.
     * @param usage The usage used by this operation.
     * @param finalAgentId The final agent id used by this operation.
     */
    public AgentRunResult(String output, TokenUsage usage, String finalAgentId)
    {
        this.output = Objects.requireNonNull(output, "output");
        this.usage = Objects.requireNonNull(usage, "usage");
        this.finalAgentId = Objects.requireNonNull(finalAgentId, "finalAgentId");
    }
}
