package stark.dataworks.coderaider.gundam.core.memory.policy;

import java.util.List;

import stark.dataworks.coderaider.gundam.core.model.Message;

/**
 * Applies optional lifecycle controls (compaction/retention/namespacing) to memory snapshots.
 */
public interface MemoryLifecyclePolicy
{
    List<Message> onRead(String agentId, String sessionId, List<Message> messages);

    List<Message> onWrite(String agentId, String sessionId, List<Message> messages);

    static MemoryLifecyclePolicy noop()
    {
        return new MemoryLifecyclePolicy()
        {
            @Override
            public List<Message> onRead(String agentId, String sessionId, List<Message> messages)
            {
                return messages;
            }

            @Override
            public List<Message> onWrite(String agentId, String sessionId, List<Message> messages)
            {
                return messages;
            }
        };
    }
}
