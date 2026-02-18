package stark.dataworks.coderaider.gundam.core.tracing;

import java.time.Instant;
import java.util.Map;

public record TraceEvent(String spanName, Instant timestamp, Map<String, String> attributes) {
}
