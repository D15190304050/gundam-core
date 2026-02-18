package stark.dataworks.coderaider.gundam.core.memory;

import java.util.List;

import stark.dataworks.coderaider.gundam.core.model.Message;
/**
 * Interface IAgentMemory.
 */

public interface IAgentMemory
{
    /**
     * Executes messages.
     */
    List<Message> messages();
    /**
     * Executes append.
     */

    void append(Message message);
}
