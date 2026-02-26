package stark.dataworks.coderaider.gundam.core.memory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.session.SessionItem;

/**
 * OpenAI-style memory that retains both plain messages and itemized conversation entries.
 */
public class OpenAiLikeAgentMemory implements IAgentMemory
{
    private final List<Message> messages = new ArrayList<>();
    private final List<SessionItem> items = new ArrayList<>();

    @Override
    public List<Message> messages()
    {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public void append(Message message)
    {
        messages.add(message);
        items.add(new SessionItem(
            "item_" + UUID.randomUUID().toString().replace("-", ""),
            message.getRole().name().toLowerCase(),
            message.getContent(),
            Instant.now(),
            Map.of("source", "runtime")));
    }

    public void appendItem(SessionItem item)
    {
        if (item == null)
        {
            return;
        }
        items.add(item);
        messages.add(new Message(stark.dataworks.coderaider.gundam.core.model.Role.valueOf(item.getRole().toUpperCase()), item.getContent()));
    }

    public List<SessionItem> items()
    {
        return Collections.unmodifiableList(items);
    }
}
