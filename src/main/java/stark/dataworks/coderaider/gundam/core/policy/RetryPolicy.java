package stark.dataworks.coderaider.gundam.core.policy;

/**
 * RetryPolicy implements core runtime responsibilities.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class RetryPolicy
{

    /**
     * Internal state for max attempts; used while coordinating runtime behavior.
     */
    private final int maxAttempts;

    /**
     * Internal state for backoff millis; used while coordinating runtime behavior.
     */
    private final long backoffMillis;

    /**
     * Performs retry policy as part of RetryPolicy runtime responsibilities.
     * @param maxAttempts The max attempts used by this operation.
     * @param backoffMillis The backoff millis used by this operation.
     */
    public RetryPolicy(int maxAttempts, long backoffMillis)
    {
        if (maxAttempts < 1)
        {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        this.maxAttempts = maxAttempts;
        this.backoffMillis = Math.max(backoffMillis, 0);
    }

    /**
     * Performs none as part of RetryPolicy runtime responsibilities.
     * @return The value produced by this operation.
     */
    public static RetryPolicy none()
    {
        return new RetryPolicy(1, 0);
    }

    /**
     * Returns the current max attempts value maintained by this RetryPolicy.
     * @return The value produced by this operation.
     */
    public int getMaxAttempts()
    {
        return maxAttempts;
    }

    /**
     * Returns the current backoff millis value maintained by this RetryPolicy.
     * @return The value produced by this operation.
     */
    public long getBackoffMillis()
    {
        return backoffMillis;
    }
}
