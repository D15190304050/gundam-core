package stark.dataworks.coderaider.gundam.core.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stark.dataworks.coderaider.gundam.core.model.Message;
/**
 * Class InMemoryAgentMemory.
 */

public class InMemoryAgentMemory implements IAgentMemory
{
    /**
     * Field messages.
     */
    private final List<Message> messages = new ArrayList<>();

    /**
     * Executes messages.
     */
    @Override
    public List<Message> messages()
    {
        return Collections.unmodifiableList(messages);
    }

    /**
     * Executes append.
     */
    @Override
    public void append(Message message)
    {
        messages.add(message);
    }
}
