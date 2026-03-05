package stark.dataworks.coderaider.gundam.core.session;

import java.util.List;

import lombok.Getter;
import stark.dataworks.coderaider.gundam.core.model.Message;

/**
 * Session implements session persistence and restoration.
 */
@Getter
public class Session
{
    /**
     * Unique identifier for this definition.
     */
    private final String id;

    /**
     * Conversation messages persisted in this session/request.
     */
    private final List<Message> messages;

    public Session(String id, List<Message> messages)
    {
        this.id = id;
        this.messages = List.copyOf(messages);
    }
}
