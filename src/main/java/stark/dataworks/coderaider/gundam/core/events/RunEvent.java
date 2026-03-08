package stark.dataworks.coderaider.gundam.core.events;

import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * RunEvent implements run event payloads.
 */
@Getter
public class RunEvent
{

    /**
     * Type discriminator for this item/event/span.
     */
    private final RunEventType type;

    /**
     * Creation time in epoch milliseconds.
     */
    private final Instant timestamp;

    /**
     * Additional key-value payload fields.
     */
    private final Map<String, Object> attributes;

    /**
     * Initializes RunEvent with required runtime dependencies and options.
     *
     * @param type       type discriminator.
     * @param attributes attribute map.
     */
    public RunEvent(RunEventType type, Map<String, Object> attributes)
    {
        this.type = Objects.requireNonNull(type, "type");
        this.timestamp = Instant.now();
        this.attributes = Collections.unmodifiableMap(attributes == null ? Map.of() : attributes);
    }
}
