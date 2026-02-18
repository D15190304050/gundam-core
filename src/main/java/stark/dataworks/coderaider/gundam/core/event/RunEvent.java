package stark.dataworks.coderaider.gundam.core.event;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * RunEvent implements run event payloads.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class RunEvent
{

    /**
     * Internal state for type; used while coordinating runtime behavior.
     */
    private final RunEventType type;

    /**
     * Internal state for timestamp; used while coordinating runtime behavior.
     */
    private final Instant timestamp;

    /**
     * Internal state for attributes; used while coordinating runtime behavior.
     */
    private final Map<String, Object> attributes;

    /**
     * Performs run event as part of RunEvent runtime responsibilities.
     * @param type The type used by this operation.
     * @param attributes The attributes used by this operation.
     */
    public RunEvent(RunEventType type, Map<String, Object> attributes)
    {
        this.type = Objects.requireNonNull(type, "type");
        this.timestamp = Instant.now();
        this.attributes = Collections.unmodifiableMap(attributes == null ? Map.of() : attributes);
    }

    /**
     * Returns the current type value maintained by this RunEvent.
     * @return The value produced by this operation.
     */
    public RunEventType getType()
    {
        return type;
    }

    /**
     * Returns the current timestamp value maintained by this RunEvent.
     * @return The value produced by this operation.
     */
    public Instant getTimestamp()
    {
        return timestamp;
    }

    /**
     * Returns the current attributes value maintained by this RunEvent.
     * @return The value produced by this operation.
     */
    public Map<String, Object> getAttributes()
    {
        return attributes;
    }
}
