package stark.dataworks.coderaider.gundam.core.model;

import java.util.Objects;

/**
 * Message implements core runtime responsibilities.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class Message
{

    /**
     * Internal state for role; used while coordinating runtime behavior.
     */
    private final Role role;

    /**
     * Internal state for content; used while coordinating runtime behavior.
     */
    private final String content;

    /**
     * Performs message as part of Message runtime responsibilities.
     * @param role The role used by this operation.
     * @param content The content used by this operation.
     */
    public Message(Role role, String content)
    {
        this.role = Objects.requireNonNull(role, "role");
        this.content = Objects.requireNonNull(content, "content");
    }

    /**
     * Returns the current role value maintained by this Message.
     * @return The value produced by this operation.
     */
    public Role getRole()
    {
        return role;
    }

    /**
     * Returns the current content value maintained by this Message.
     * @return The value produced by this operation.
     */
    public String getContent()
    {
        return content;
    }
}
