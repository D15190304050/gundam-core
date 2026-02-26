package stark.dataworks.coderaider.gundam.core.session;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;
import stark.dataworks.coderaider.gundam.core.model.Message;

/**
 * Session implements session persistence and restoration.
 */
@Getter
public class Session
{
    /**
     * Internal state for id; used while coordinating runtime behavior.
     */
    private final String id;

    /**
     * Internal state for messages; used while coordinating runtime behavior.
     */
    private final List<Message> messages;

    /**
     * OpenAI-like serialized items with ids/timestamps and metadata.
     */
    private final List<SessionItem> items;

    /**
     * Optional id of the latest response associated with this persisted session.
     */
    private final String lastResponseId;

    public Session(String id, List<Message> messages)
    {
        this(id, messages, toItems(messages), null);
    }

    public Session(String id, List<Message> messages, List<SessionItem> items, String lastResponseId)
    {
        this.id = id;
        this.messages = List.copyOf(messages);
        this.items = items == null || items.isEmpty() ? toItems(messages) : List.copyOf(items);
        this.lastResponseId = lastResponseId;
    }

    private static List<SessionItem> toItems(List<Message> messages)
    {
        Instant now = Instant.now();
        return messages.stream()
            .map(message -> new SessionItem(
                "item_" + UUID.randomUUID().toString().replace("-", ""),
                message.getRole().name().toLowerCase(),
                message.getContent(),
                now,
                Map.of("source", "memory")))
            .collect(Collectors.toList());
    }
}
