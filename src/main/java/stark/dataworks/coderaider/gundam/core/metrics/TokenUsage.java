package stark.dataworks.coderaider.gundam.core.metrics;

import lombok.Getter;

/**
 * TokenUsage implements token usage tracking.
 */
@Getter
public class TokenUsage
{

    /**
     * Input token count reported by the model provider.
     */
    private final int inputTokens;

    /**
     * Output token count reported by the model provider.
     */
    private final int outputTokens;

    /**
     * Initializes TokenUsage with required runtime dependencies and options.
     * @param inputTokens input tokens.
     * @param outputTokens output tokens.
     */
    public TokenUsage(int inputTokens, int outputTokens)
    {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }

    /**
     * Returns total tokens.
     * @return Computed numeric result.
     */
    public int getTotalTokens()
    {
        return inputTokens + outputTokens;
    }
}
