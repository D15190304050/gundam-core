package stark.dataworks.coderaider.genericagent.core.memory.context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import stark.dataworks.coderaider.genericagent.core.context.ContextItem;

/**
 * In-memory default context-service store for local runs/tests.
 */
public class InMemoryContextServiceMemoryStore implements IContextServiceMemoryStore
{
    private final Map<String, List<ContextItem>> data = new ConcurrentHashMap<>();

    @Override
    public ContextReadResult read(String namespace, String sessionId)
    {
        List<ContextItem> messages = data.get(key(namespace, sessionId));
        return new ContextReadResult(messages == null ? List.of() : List.copyOf(messages), messages != null);
    }

    @Override
    public void write(String namespace, String sessionId, List<ContextItem> messages)
    {
        data.put(key(namespace, sessionId), List.copyOf(messages));
    }

    private String key(String namespace, String sessionId)
    {
        return (namespace == null ? "default" : namespace) + "::" + (sessionId == null ? "default" : sessionId);
    }
}
