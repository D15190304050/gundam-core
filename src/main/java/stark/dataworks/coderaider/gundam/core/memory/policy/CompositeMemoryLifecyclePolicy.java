package stark.dataworks.coderaider.gundam.core.memory.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.model.Message;

/**
 * Composes multiple lifecycle policies.
 */
public class CompositeMemoryLifecyclePolicy implements MemoryLifecyclePolicy
{
    private final List<MemoryLifecyclePolicy> delegates;

    public CompositeMemoryLifecyclePolicy(List<MemoryLifecyclePolicy> delegates)
    {
        this.delegates = delegates == null ? List.of() : List.copyOf(delegates);
    }

    @Override
    public List<Message> onRead(String agentId, String sessionId, List<Message> messages)
    {
        List<Message> result = messages;
        for (MemoryLifecyclePolicy delegate : delegates)
        {
            result = Objects.requireNonNull(delegate, "delegate").onRead(agentId, sessionId, result);
        }
        return result;
    }

    @Override
    public List<Message> onWrite(String agentId, String sessionId, List<Message> messages)
    {
        List<Message> result = messages;
        for (MemoryLifecyclePolicy delegate : delegates)
        {
            result = Objects.requireNonNull(delegate, "delegate").onWrite(agentId, sessionId, result);
        }
        return result;
    }

    public static CompositeMemoryLifecyclePolicy of(MemoryLifecyclePolicy... policies)
    {
        List<MemoryLifecyclePolicy> list = new ArrayList<>();
        if (policies != null)
        {
            for (MemoryLifecyclePolicy policy : policies)
            {
                if (policy != null)
                {
                    list.add(policy);
                }
            }
        }
        return new CompositeMemoryLifecyclePolicy(list);
    }
}
