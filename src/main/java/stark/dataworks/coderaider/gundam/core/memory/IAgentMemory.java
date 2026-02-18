package stark.dataworks.coderaider.gundam.core.memory;

import java.util.List;

import stark.dataworks.coderaider.gundam.core.model.Message;

/**
 * IAgentMemory implements conversation state retention between turns.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
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
}
