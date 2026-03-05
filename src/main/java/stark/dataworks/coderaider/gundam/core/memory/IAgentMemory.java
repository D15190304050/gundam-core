package stark.dataworks.coderaider.gundam.core.memory;

import java.util.List;

import stark.dataworks.coderaider.gundam.core.model.Message;

/**
 * IAgentMemory implements conversation state retention between turns.
 */
public interface IAgentMemory
{

    /**
     * Returns the message history for this memory backend.
     * @return List of message values.
     */
    List<Message> messages();

    /**
     * Adds data to internal state consumed by later runtime steps.
     * @param message conversation message.
     */

    void append(Message message);

    /**
     * Replaces all messages when lifecycle policies compact/trim memory state.
     * Implementations that do not support mutation can throw {@link UnsupportedOperationException}.
     * @param messages The full normalized message list.
     */
    default void replaceAll(List<Message> messages)
    {
        throw new UnsupportedOperationException("replaceAll is not supported");
    }
}
