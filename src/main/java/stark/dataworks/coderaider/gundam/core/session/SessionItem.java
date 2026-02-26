package stark.dataworks.coderaider.gundam.core.session;

import java.time.Instant;
import java.util.Map;

import lombok.Getter;

/**
 * OpenAI-like persisted conversation item.
 */
@Getter
public class SessionItem
{
    private final String itemId;
    private final String role;
    private final String content;
    private final Instant createdAt;
    private final Map<String, Object> metadata;

    public SessionItem(String itemId, String role, String content, Instant createdAt, Map<String, Object> metadata)
    {
        this.itemId = itemId;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
