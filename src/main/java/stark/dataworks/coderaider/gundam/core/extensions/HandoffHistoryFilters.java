package stark.dataworks.coderaider.gundam.core.extensions;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.model.Role;

/**
 * Utility filters for transferring bounded conversation history during agent handoff.
 */
public final class HandoffHistoryFilters
{
    private HandoffHistoryFilters()
    {
    }

    public static List<Message> removeToolMessages(List<Message> source)
    {
        Objects.requireNonNull(source, "source");
        return source.stream().filter(message -> message.getRole() != Role.TOOL).collect(Collectors.toList());
    }

    public static List<Message> lastNMessages(List<Message> source, int limit)
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

    public static List<Message> filter(List<Message> source, Predicate<Message> predicate)
    {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(predicate, "predicate");
        return source.stream().filter(predicate).collect(Collectors.toList());
    }
}
