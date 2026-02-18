package stark.dataworks.coderaider.gundam.core.metrics;
/**
 * Class TokenUsage.
 */

public class TokenUsage
{
    /**
     * Field inputTokens.
     */
    private final int inputTokens;
    /**
     * Field outputTokens.
     */
    private final int outputTokens;
    /**
     * Creates a new TokenUsage instance.
     */

    public TokenUsage(int inputTokens, int outputTokens)
    {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }
    /**
     * Executes getInputTokens.
     */

    public int getInputTokens()
    {
        return inputTokens;
    }
    /**
     * Executes getOutputTokens.
     */

    public int getOutputTokens()
    {
        return outputTokens;
    }
    /**
     * Executes getTotalTokens.
     */

    public int getTotalTokens()
    {
        return inputTokens + outputTokens;
    }
}
