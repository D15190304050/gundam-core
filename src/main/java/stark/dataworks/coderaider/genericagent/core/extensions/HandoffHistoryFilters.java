package stark.dataworks.coderaider.genericagent.core.extensions;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import stark.dataworks.coderaider.genericagent.core.context.ContextItem;
import stark.dataworks.coderaider.genericagent.core.model.Role;

/**
 * Utility filters for transferring bounded conversation history during agent handoff.
 */
public final class HandoffHistoryFilters
{
    private HandoffHistoryFilters()
    {
    }

    public static List<ContextItem> removeToolMessages(List<ContextItem> source)
    {
        Objects.requireNonNull(source, "source");
        return source.stream().filter(message -> message.getRole() != Role.TOOL).collect(Collectors.toList());
    }

    public static List<ContextItem> lastNMessages(List<ContextItem> source, int limit)
    {
        Objects.requireNonNull(source, "source");
        if (limit < 0)
        {
            throw new IllegalArgumentException("limit must be >= 0");
        }
        if (source.size() <= limit)
        {
            return List.copyOf(source);
        }
        return List.copyOf(source.subList(source.size() - limit, source.size()));
    }

    public static List<ContextItem> filter(List<ContextItem> source, Predicate<ContextItem> predicate)
    {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(predicate, "predicate");
        return source.stream().filter(predicate).collect(Collectors.toList());
    }
}
