package stark.dataworks.coderaider.gundam.core.tracing;

/**
 * TraceSpan implements run tracing and span publication.
 */
public interface ITraceSpan
{

    /**
     * Adds an attribute to the current span.
     * @param key key.
     * @param value value.
     */
    void annotate(String key, String value);

    /**
     * Closes this value.
     */

    void close();
}
