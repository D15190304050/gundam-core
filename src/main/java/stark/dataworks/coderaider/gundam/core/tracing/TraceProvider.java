package stark.dataworks.coderaider.gundam.core.tracing;

/**
 * TraceProvider implements run tracing and span publication.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface TraceProvider
{

    /**
     * Performs start span as part of TraceProvider runtime responsibilities.
     * @param name The name used by this operation.
     * @return The value produced by this operation.
     */
    TraceSpan startSpan(String name);
}
