package stark.dataworks.coderaider.gundam.core.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import stark.dataworks.coderaider.gundam.core.model.Message;

public class InMemoryAgentMemory implements IAgentMemory {
    private final List<Message> messages = new ArrayList<>();

    @Override
    public List<Message> messages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public void append(Message message) {
        messages.add(message);
    }
}
