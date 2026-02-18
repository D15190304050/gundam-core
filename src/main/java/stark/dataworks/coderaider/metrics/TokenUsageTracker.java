package stark.dataworks.coderaider.metrics;

public class TokenUsageTracker {
    private int input;
    private int output;

    public void add(TokenUsage usage) {
        this.input += usage.getInputTokens();
        this.output += usage.getOutputTokens();
    }

    public TokenUsage snapshot() {
        return new TokenUsage(input, output);
    }
}
