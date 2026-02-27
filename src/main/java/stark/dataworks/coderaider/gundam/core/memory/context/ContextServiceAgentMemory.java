package stark.dataworks.coderaider.gundam.core.memory.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.model.Message;

/**
 * Memory implementation backed by external context-service store.
 */
public class ContextServiceAgentMemory implements IAgentMemory
{
    private final String namespace;
    private final String sessionId;
    private final IContextServiceMemoryStore store;
    private final List<Message> writes = new ArrayList<>();

    public ContextServiceAgentMemory(String namespace, String sessionId, IContextServiceMemoryStore store)
    {
        this.namespace = namespace;
        this.sessionId = sessionId;
        this.store = Objects.requireNonNull(store, "store");
    }

    @Override
    public List<Message> messages()
    {
        List<Message> baseline = store.read(namespace, sessionId).messages();
        if (writes.isEmpty())
        {
            return baseline;
        }
        List<Message> merged = new ArrayList<>(baseline);
        merged.addAll(writes);
        return merged;
    }

    @Override
    public void append(Message message)
    {
        writes.add(message);
        store.write(namespace, sessionId, messages());
    }

    @Override
    public void replaceAll(List<Message> messages)
    {
        writes.clear();
        store.write(namespace, sessionId, messages);
    }
}
