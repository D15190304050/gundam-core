package stark.dataworks.coderaider.gundam.core.metrics;

/**
 * TokenUsage implements token usage tracking.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class TokenUsage
{

    /**
     * Internal state for input tokens; used while coordinating runtime behavior.
     */
    private final int inputTokens;

    /**
     * Internal state for output tokens; used while coordinating runtime behavior.
     */
    private final int outputTokens;

    /**
     * Performs token usage as part of TokenUsage runtime responsibilities.
     * @param inputTokens The input tokens used by this operation.
     * @param outputTokens The output tokens used by this operation.
     */
    public TokenUsage(int inputTokens, int outputTokens)
    {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }

    /**
     * Returns the current input tokens value maintained by this TokenUsage.
     * @return The value produced by this operation.
     */
    public int getInputTokens()
    {
        return inputTokens;
    }

    /**
     * Returns the current output tokens value maintained by this TokenUsage.
     * @return The value produced by this operation.
     */
    public int getOutputTokens()
    {
        return outputTokens;
    }

    /**
     * Returns the current total tokens value maintained by this TokenUsage.
     * @return The value produced by this operation.
     */
    public int getTotalTokens()
    {
        return inputTokens + outputTokens;
    }
}
