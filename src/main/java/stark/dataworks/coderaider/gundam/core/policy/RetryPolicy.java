package stark.dataworks.coderaider.gundam.core.policy;
/**
 * Class RetryPolicy.
 */

public class RetryPolicy
{
    /**
     * Field maxAttempts.
     */
    private final int maxAttempts;
    /**
     * Field backoffMillis.
     */
    private final long backoffMillis;
    /**
     * Creates a new RetryPolicy instance.
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
     * Executes none.
     */

    public static RetryPolicy none()
    {
        return new RetryPolicy(1, 0);
    }
    /**
     * Executes getMaxAttempts.
     */

    public int getMaxAttempts()
    {
        return maxAttempts;
    }
    /**
     * Executes getBackoffMillis.
     */

    public long getBackoffMillis()
    {
        return backoffMillis;
    }
}
