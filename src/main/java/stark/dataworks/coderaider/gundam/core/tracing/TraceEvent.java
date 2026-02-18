package stark.dataworks.coderaider.gundam.core.tracing;

import java.time.Instant;
import java.util.Map;

/**
 * TraceEvent implements run tracing and span publication.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public record TraceEvent(String spanName, Instant timestamp, Map<String, String> attributes)
{
}
