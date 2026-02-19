package stark.dataworks.coderaider.gundam.core.realtime;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;

/**
 * Realtime session event payload.
 */
@Getter
public class RealtimeEvent
{
    private final RealtimeEventType type;
    private final Instant at;
    private final Map<String, Object> data;

    public RealtimeEvent(RealtimeEventType type, Instant at, Map<String, Object> data)
    {
        this.type = Objects.requireNonNull(type, "type");
        this.at = at == null ? Instant.now() : at;
        this.data = data == null ? Map.of() : Map.copyOf(data);
    }
}
