package stark.dataworks.coderaider.genericagent.core.memory.policy;

import java.util.ArrayList;
import java.util.List;

import stark.dataworks.coderaider.genericagent.core.context.ContextItem;

/**
 * Retains only the last N messages.
 */
public class SlidingWindowMemoryPolicy implements MemoryLifecyclePolicy
{
    private final int maxMessages;

    public SlidingWindowMemoryPolicy(int maxMessages)
    {
        if (maxMessages < 1)
        {
            throw new IllegalArgumentException("maxMessages must be >= 1");
        }
        this.maxMessages = maxMessages;
    }

    @Override
    public List<ContextItem> onRead(String agentId, String sessionId, List<ContextItem> messages)
    {
        return trim(messages);
    }

    @Override
    public List<ContextItem> onWrite(String agentId, String sessionId, List<ContextItem> messages)
    {
        return trim(messages);
    }

    private List<ContextItem> trim(List<ContextItem> messages)
    {
        if (messages.size() <= maxMessages)
        {
            return messages;
        }
        return new ArrayList<>(messages.subList(messages.size() - maxMessages, messages.size()));
    }
}
