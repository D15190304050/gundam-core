package stark.dataworks.coderaider.genericagent.core.memory.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import stark.dataworks.coderaider.genericagent.core.memory.IAgentMemory;
import stark.dataworks.coderaider.genericagent.core.context.ContextItem;

/**
 * Memory implementation backed by external context-service store.
 */
public class ContextServiceAgentMemory implements IAgentMemory
{
    private final String namespace;
    private final String sessionId;
    private final IContextServiceMemoryStore store;
    private final List<ContextItem> writes = new ArrayList<>();

    public ContextServiceAgentMemory(String namespace, String sessionId, IContextServiceMemoryStore store)
    {
        this.namespace = namespace;
        this.sessionId = sessionId;
        this.store = Objects.requireNonNull(store, "store");
    }

    @Override
    public List<ContextItem> messages()
    {
        List<ContextItem> baseline = store.read(namespace, sessionId).messages();
        if (writes.isEmpty())
        {
            return baseline;
        }
        List<ContextItem> merged = new ArrayList<>(baseline);
        merged.addAll(writes);
        return merged;
    }

    @Override
    public void append(ContextItem message)
    {
        writes.add(message);
        store.write(namespace, sessionId, messages());
    }

    @Override
    public void replaceAll(List<ContextItem> messages)
    {
        writes.clear();
        store.write(namespace, sessionId, messages);
    }
}
