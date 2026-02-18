package stark.dataworks.coderaider.gundam.core.tracing;
/**
 * Interface TraceSpan.
 */

public interface TraceSpan
{
    /**
     * Executes annotate.
     */
    void annotate(String key, String value);
    /**
     * Executes close.
     */

    void close();
}
