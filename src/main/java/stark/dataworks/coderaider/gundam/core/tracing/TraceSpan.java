package stark.dataworks.coderaider.gundam.core.tracing;

/**
 * TraceSpan implements run tracing and span publication.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface TraceSpan
{

    /**
     * Performs annotate as part of TraceSpan runtime responsibilities.
     * @param key The key used by this operation.
     * @param value The value used by this operation.
     */
    void annotate(String key, String value);

    /**
     * Performs close as part of TraceSpan runtime responsibilities.
     */

    void close();
}
