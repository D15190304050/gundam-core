package stark.dataworks.coderaider.gundam.core.session;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.model.Message;
/**
 * Class Session.
 */

public class Session
{
    /**
     * Field id.
     */
    private final String id;
    /**
     * Field messages.
     */
    private final List<Message> messages;
    /**
     * Creates a new Session instance.
     */

    public Session(String id, List<Message> messages)
    {
        this.id = Objects.requireNonNull(id, "id");
        this.messages = Collections.unmodifiableList(Objects.requireNonNull(messages, "messages"));
    }
    /**
     * Executes getId.
     */

    public String getId()
    {
        return id;
    }
    /**
     * Executes getMessages.
     */

    public List<Message> getMessages()
    {
        return messages;
    }
}
