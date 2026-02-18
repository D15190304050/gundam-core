package stark.dataworks.coderaider.gundam.core.metrics;

/**
 * TokenUsageTracker implements token usage tracking.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class TokenUsageTracker
{

    /**
     * Internal state for input; used while coordinating runtime behavior.
     */
    private int input;

    /**
     * Internal state for output; used while coordinating runtime behavior.
     */
    private int output;

    /**
     * Adds data to internal state consumed by later runtime steps.
     * @param usage The usage used by this operation.
     */
    public void add(TokenUsage usage)
    {
        this.input += usage.getInputTokens();
        this.output += usage.getOutputTokens();
    }

    /**
     * Performs snapshot as part of TokenUsageTracker runtime responsibilities.
     * @return The value produced by this operation.
     */
    public TokenUsage snapshot()
    {
        return new TokenUsage(input, output);
    }
}
