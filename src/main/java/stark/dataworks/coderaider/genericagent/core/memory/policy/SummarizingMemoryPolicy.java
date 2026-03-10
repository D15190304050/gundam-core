package stark.dataworks.coderaider.genericagent.core.memory.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import stark.dataworks.coderaider.genericagent.core.context.ContextItem;
import stark.dataworks.coderaider.genericagent.core.model.Role;

/**
 * Simple compaction policy that prepends a generated summary when conversation exceeds threshold.
 */
public class SummarizingMemoryPolicy implements MemoryLifecyclePolicy
{
    private final int threshold;

    public SummarizingMemoryPolicy(int threshold)
    {
        if (threshold < 2)
        {
            throw new IllegalArgumentException("threshold must be >= 2");
        }
        this.threshold = threshold;
    }

    @Override
    public List<ContextItem> onRead(String agentId, String sessionId, List<ContextItem> messages)
    {
        return compact(messages);
    }

    @Override
    public List<ContextItem> onWrite(String agentId, String sessionId, List<ContextItem> messages)
    {
        return compact(messages);
    }

    private List<ContextItem> compact(List<ContextItem> messages)
    {
        if (messages.size() <= threshold)
        {
            return messages;
        }
        int split = Math.max(1, messages.size() / 2);
        String summary = messages.subList(0, split).stream()
            .map(m -> m.getRole() + ": " + sanitize(m.getContent()))
            .collect(Collectors.joining(" | "));
        List<ContextItem> output = new ArrayList<>();
        output.add(new ContextItem(Role.SYSTEM, "Summary of prior context: " + summary));
        output.addAll(messages.subList(split, messages.size()));
        return output;
    }

    private String sanitize(String content)
    {
        if (content == null)
        {
            return "";
        }
        return content.length() <= 80 ? content : content.substring(0, 80) + "...";
    }
}
