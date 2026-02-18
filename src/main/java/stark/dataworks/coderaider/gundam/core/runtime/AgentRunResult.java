package stark.dataworks.coderaider.gundam.core.runtime;

import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
/**
 * Class AgentRunResult.
 */

public class AgentRunResult
{
    /**
     * Field output.
     */
    private final String output;
    /**
     * Field usage.
     */
    private final TokenUsage usage;
    /**
     * Field finalAgentId.
     */
    private final String finalAgentId;
    /**
     * Creates a new AgentRunResult instance.
     */

    public AgentRunResult(String output, TokenUsage usage, String finalAgentId)
    {
        this.output = Objects.requireNonNull(output, "output");
        this.usage = Objects.requireNonNull(usage, "usage");
        this.finalAgentId = Objects.requireNonNull(finalAgentId, "finalAgentId");
    }
    /**
     * Executes getOutput.
     */

    public String getOutput()
    {
        return output;
    }
    /**
     * Executes getUsage.
     */

    public TokenUsage getUsage()
    {
        return usage;
    }
    /**
     * Executes getFinalAgentId.
     */

    public String getFinalAgentId()
    {
        return finalAgentId;
    }
}
