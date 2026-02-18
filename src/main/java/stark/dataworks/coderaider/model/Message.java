package stark.dataworks.coderaider.model;

import java.util.Objects;

public class Message {
    private final Role role;
    private final String content;

    public Message(Role role, String content) {
        this.role = Objects.requireNonNull(role, "role");
        this.content = Objects.requireNonNull(content, "content");
    }

    public Role getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}
