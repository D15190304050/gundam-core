package stark.dataworks.coderaider.gundam.core.memory.context;

import java.util.List;

import stark.dataworks.coderaider.gundam.core.model.Message;

/**
 * SPI for external context services used as conversation-memory backends.
 */
public interface IContextServiceMemoryStore
{
    ContextReadResult read(String namespace, String sessionId);

    void write(String namespace, String sessionId, List<Message> messages);

    record ContextReadResult(List<Message> messages, boolean cacheHit)
    {
    }
}
