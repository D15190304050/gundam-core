package stark.dataworks.coderaider.genericagent.core.memory;

import java.util.List;

import stark.dataworks.coderaider.genericagent.core.context.ContextItem;

/**
 * IAgentMemory implements conversation state retention between turns.
 */
public interface IAgentMemory
{

    /**
     * Returns the message history for this memory backend.
     *
     * @return List of message values.
     */
    List<ContextItem> messages();

    /**
     * Adds data to internal state consumed by later runtime steps.
     *
     * @param message conversation message.
     */

    void append(ContextItem message);

    /**
     * Replaces all messages when lifecycle policies compact/trim memory state.
     * Implementations that do not support mutation can throw {@link UnsupportedOperationException}.
     *
     * @param messages The full normalized message list.
     */
    default void replaceAll(List<ContextItem> messages)
    {
        throw new UnsupportedOperationException("replaceAll is not supported");
    }
}
