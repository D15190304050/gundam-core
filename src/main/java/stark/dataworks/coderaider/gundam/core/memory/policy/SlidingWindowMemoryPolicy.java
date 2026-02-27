package stark.dataworks.coderaider.gundam.core.memory.policy;

import java.util.ArrayList;
import java.util.List;

import stark.dataworks.coderaider.gundam.core.model.Message;

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
    public List<Message> onRead(String agentId, String sessionId, List<Message> messages)
    {
        return trim(messages);
    }

    @Override
    public List<Message> onWrite(String agentId, String sessionId, List<Message> messages)
    {
        return trim(messages);
    }

    private List<Message> trim(List<Message> messages)
    {
        if (messages.size() <= maxMessages)
        {
            return messages;
        }
        return new ArrayList<>(messages.subList(messages.size() - maxMessages, messages.size()));
    }
}
