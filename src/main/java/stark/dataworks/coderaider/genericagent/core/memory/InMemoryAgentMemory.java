package stark.dataworks.coderaider.genericagent.core.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stark.dataworks.coderaider.genericagent.core.context.ContextItem;

/**
 * In-memory memory that retains conversation messages.
 */
public class InMemoryAgentMemory implements IAgentMemory
{
    private final List<ContextItem> messages = new ArrayList<>();

    @Override
    public List<ContextItem> messages()
    {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public void append(ContextItem message)
    {
        messages.add(message);
    }

    @Override
    public void replaceAll(List<ContextItem> newMessages)
    {
        messages.clear();
        messages.addAll(newMessages);
    }
}
