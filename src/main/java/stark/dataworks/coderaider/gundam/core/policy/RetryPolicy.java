package stark.dataworks.coderaider.gundam.core.policy;

import lombok.Getter;

/**
 * RetryPolicy implements core runtime responsibilities.
 */
@Getter
public class RetryPolicy
{

    /**
     * Maximum retry attempts before giving up.
     */
    private final int maxAttempts;

    /**
     * Backoff interval (millis.
     */
    private final long backoffMillis;

    /**
     * Initializes RetryPolicy with required runtime dependencies and options.
     * @param maxAttempts max attempts.
     * @param backoffMillis backoff millis.
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
     * Returns a retry policy that disables retries.
     * @return retry policy result.
     */
    public static RetryPolicy none()
    {
        return new RetryPolicy(1, 0);
    }
}
