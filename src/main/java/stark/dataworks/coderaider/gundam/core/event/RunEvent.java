package stark.dataworks.coderaider.gundam.core.event;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
/**
 * Class RunEvent.
 */

public class RunEvent
{
    /**
     * Field type.
     */
    private final RunEventType type;
    /**
     * Field timestamp.
     */
    private final Instant timestamp;
    /**
     * Field attributes.
     */
    private final Map<String, Object> attributes;
    /**
     * Creates a new RunEvent instance.
     */

    public RunEvent(RunEventType type, Map<String, Object> attributes)
    {
        this.type = Objects.requireNonNull(type, "type");
        this.timestamp = Instant.now();
        this.attributes = Collections.unmodifiableMap(attributes == null ? Map.of() : attributes);
    }
    /**
     * Executes getType.
     */

    public RunEventType getType()
    {
        return type;
    }
    /**
     * Executes getTimestamp.
     */

    public Instant getTimestamp()
    {
        return timestamp;
    }
    /**
     * Executes getAttributes.
     */

    public Map<String, Object> getAttributes()
    {
        return attributes;
    }
}
