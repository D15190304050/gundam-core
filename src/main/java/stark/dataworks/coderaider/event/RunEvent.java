package stark.dataworks.coderaider.event;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class RunEvent {
    private final RunEventType type;
    private final Instant timestamp;
    private final Map<String, Object> attributes;

    public RunEvent(RunEventType type, Map<String, Object> attributes) {
        this.type = Objects.requireNonNull(type, "type");
        this.timestamp = Instant.now();
        this.attributes = Collections.unmodifiableMap(attributes == null ? Map.of() : attributes);
    }

    public RunEventType getType() {
        return type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
