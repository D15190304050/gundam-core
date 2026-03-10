package stark.dataworks.coderaider.genericagent.core.memory.policy;

import java.util.List;

import stark.dataworks.coderaider.genericagent.core.context.ContextItem;

/**
 * Applies optional lifecycle controls (compaction/retention/namespacing) to memory snapshots.
 */
public interface MemoryLifecyclePolicy
{
    List<ContextItem> onRead(String agentId, String sessionId, List<ContextItem> messages);

    List<ContextItem> onWrite(String agentId, String sessionId, List<ContextItem> messages);

    static MemoryLifecyclePolicy noop()
    {
        return new MemoryLifecyclePolicy()
        {
            @Override
            public List<ContextItem> onRead(String agentId, String sessionId, List<ContextItem> messages)
            {
                return messages;
            }

            @Override
            public List<ContextItem> onWrite(String agentId, String sessionId, List<ContextItem> messages)
            {
                return messages;
            }
        };
    }
}
