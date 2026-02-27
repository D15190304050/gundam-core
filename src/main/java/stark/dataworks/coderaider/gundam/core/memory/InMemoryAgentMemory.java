package stark.dataworks.coderaider.gundam.core.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stark.dataworks.coderaider.gundam.core.model.Message;

/**
 * In-memory memory that retains conversation messages.
 */
public class InMemoryAgentMemory implements IAgentMemory
{
    private final List<Message> messages = new ArrayList<>();

    @Override
    public List<Message> messages()
    {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public void append(Message message)
    {
        messages.add(message);
    }

    @Override
    public void replaceAll(List<Message> newMessages)
    {
        messages.clear();
        messages.addAll(newMessages);
    }
}
