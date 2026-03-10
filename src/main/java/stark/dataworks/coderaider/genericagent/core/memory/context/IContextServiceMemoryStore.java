package stark.dataworks.coderaider.genericagent.core.memory.context;

import java.util.List;

import stark.dataworks.coderaider.genericagent.core.context.ContextItem;

/**
 * SPI for external context services used as conversation-memory backends.
 */
public interface IContextServiceMemoryStore
{
    ContextReadResult read(String namespace, String sessionId);

    void write(String namespace, String sessionId, List<ContextItem> messages);

    record ContextReadResult(List<ContextItem> messages, boolean cacheHit)
    {
    }
}
