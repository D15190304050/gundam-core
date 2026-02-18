package stark.dataworks.coderaider.gundam.core.metrics;
/**
 * Class TokenUsageTracker.
 */

public class TokenUsageTracker
{
    /**
     * Field input.
     */
    private int input;
    /**
     * Field output.
     */
    private int output;
    /**
     * Executes add.
     */

    public void add(TokenUsage usage)
    {
        this.input += usage.getInputTokens();
        this.output += usage.getOutputTokens();
    }
    /**
     * Executes snapshot.
     */

    public TokenUsage snapshot()
    {
        return new TokenUsage(input, output);
    }
}
