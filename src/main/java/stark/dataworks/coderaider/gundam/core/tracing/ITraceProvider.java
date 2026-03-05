package stark.dataworks.coderaider.gundam.core.tracing;

/**
 * TraceProvider implements run tracing and span publication.
 */
public interface ITraceProvider
{

    /**
     * Starts span.
     * @param name name.
     * @return itrace span result.
     */
    ITraceSpan startSpan(String name);
}
