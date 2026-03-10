package stark.dataworks.coderaider.genericagent.core.session;

import java.util.List;

import lombok.Getter;
import stark.dataworks.coderaider.genericagent.core.context.ContextItem;

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
    private final List<ContextItem> messages;

    public Session(String id, List<ContextItem> messages)
    {
        this.id = id;
        this.messages = List.copyOf(messages);
    }
}
