package stark.dataworks.coderaider.policy;

public class RetryPolicy {
    private final int maxAttempts;
    private final long backoffMillis;

    public RetryPolicy(int maxAttempts, long backoffMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        this.maxAttempts = maxAttempts;
        this.backoffMillis = Math.max(backoffMillis, 0);
    }

    public static RetryPolicy none() {
        return new RetryPolicy(1, 0);
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getBackoffMillis() {
        return backoffMillis;
    }
}
