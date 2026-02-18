package stark.dataworks.coderaider.gundam.core.model;

import java.util.Objects;
/**
 * Class Message.
 */

public class Message
{
    /**
     * Field role.
     */
    private final Role role;
    /**
     * Field content.
     */
    private final String content;
    /**
     * Creates a new Message instance.
     */

    public Message(Role role, String content)
    {
        this.role = Objects.requireNonNull(role, "role");
        this.content = Objects.requireNonNull(content, "content");
    }
    /**
     * Executes getRole.
     */

    public Role getRole()
    {
        return role;
    }
    /**
     * Executes getContent.
     */

    public String getContent()
    {
        return content;
    }
}
