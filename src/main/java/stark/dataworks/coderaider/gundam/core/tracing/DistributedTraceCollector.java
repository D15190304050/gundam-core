package stark.dataworks.coderaider.gundam.core.tracing;

/**
 * Sink for distributed trace spans.
 */
@FunctionalInterface
public interface DistributedTraceCollector
{
    void collect(DistributedTraceEvent event);
}
