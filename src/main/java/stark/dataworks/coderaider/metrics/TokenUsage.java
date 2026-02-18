package stark.dataworks.coderaider.metrics;

public class TokenUsage {
    private final int inputTokens;
    private final int outputTokens;

    public TokenUsage(int inputTokens, int outputTokens) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }

    public int getInputTokens() {
        return inputTokens;
    }

    public int getOutputTokens() {
        return outputTokens;
    }

    public int getTotalTokens() {
        return inputTokens + outputTokens;
    }
}
