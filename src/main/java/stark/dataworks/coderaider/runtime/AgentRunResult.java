package stark.dataworks.coderaider.runtime;

import java.util.Objects;
import stark.dataworks.coderaider.metrics.TokenUsage;

public class AgentRunResult {
    private final String output;
    private final TokenUsage usage;
    private final String finalAgentId;

    public AgentRunResult(String output, TokenUsage usage, String finalAgentId) {
        this.output = Objects.requireNonNull(output, "output");
        this.usage = Objects.requireNonNull(usage, "usage");
        this.finalAgentId = Objects.requireNonNull(finalAgentId, "finalAgentId");
    }

    public String getOutput() {
        return output;
    }

    public TokenUsage getUsage() {
        return usage;
    }

    public String getFinalAgentId() {
        return finalAgentId;
    }
}
