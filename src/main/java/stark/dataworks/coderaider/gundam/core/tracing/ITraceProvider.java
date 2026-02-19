package stark.dataworks.coderaider.gundam.core.tracing;

/**
 * TraceProvider implements run tracing and span publication.
 */
public interface ITraceProvider
{

    /**
     * Performs start span as part of TraceProvider runtime responsibilities.
     * @param name The name used by this operation.
     * @return The value produced by this operation.
     */
    ITraceSpan startSpan(String name);
}
