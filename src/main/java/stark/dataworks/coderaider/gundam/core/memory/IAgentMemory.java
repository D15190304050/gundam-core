package stark.dataworks.coderaider.gundam.core.memory;

import java.util.List;

import stark.dataworks.coderaider.gundam.core.model.Message;

/**
 * IAgentMemory implements conversation state retention between turns.
 */
public interface IAgentMemory
{

    /**
     * Performs messages as part of IAgentMemory runtime responsibilities.
     * @return The value produced by this operation.
     */
    List<Message> messages();

    /**
     * Adds data to internal state consumed by later runtime steps.
     * @param message The message used by this operation.
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
