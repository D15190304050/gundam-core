package stark.dataworks.coderaider.gundam.core.session;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.model.Message;

/**
 * Session implements session persistence and restoration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
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
     * Performs session as part of Session runtime responsibilities.
     * @param id The id used by this operation.
     * @param messages The messages used by this operation.
     */
    public Session(String id, List<Message> messages)
    {
        this.id = Objects.requireNonNull(id, "id");
        this.messages = Collections.unmodifiableList(Objects.requireNonNull(messages, "messages"));
    }

    /**
     * Returns the current id value maintained by this Session.
     * @return The value produced by this operation.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Returns the current messages value maintained by this Session.
     * @return The value produced by this operation.
     */
    public List<Message> getMessages()
    {
        return messages;
    }
}
