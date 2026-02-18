package stark.dataworks.coderaider.gundam.core.tracing;
/**
 * Interface TraceProvider.
 */

public interface TraceProvider
{
    /**
     * Executes startSpan.
     */
    TraceSpan startSpan(String name);
}
