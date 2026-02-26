package stark.dataworks.coderaider.gundam.core.tracing;

import java.time.Instant;
import java.util.Map;

/**
 * Distributed-style trace event with parent/child relationship.
 */
public record DistributedTraceEvent(
    String traceId,
    String spanId,
    String parentSpanId,
    String spanName,
    Instant startedAt,
    Instant endedAt,
    Map<String, String> attributes)
{
}
