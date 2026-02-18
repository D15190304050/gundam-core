package stark.dataworks.coderaider.session;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import stark.dataworks.coderaider.model.Message;

public class Session {
    private final String id;
    private final List<Message> messages;

    public Session(String id, List<Message> messages) {
        this.id = Objects.requireNonNull(id, "id");
        this.messages = Collections.unmodifiableList(Objects.requireNonNull(messages, "messages"));
    }

    public String getId() {
        return id;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
