package stark.dataworks.coderaider.gundam.core.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stark.dataworks.coderaider.gundam.core.model.Message;

/**
 * InMemoryAgentMemory implements conversation state retention between turns.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class InMemoryAgentMemory implements IAgentMemory
{

    /**
     * Ordered conversation transcript retained during a run.
     */
    private final List<Message> messages = new ArrayList<>();

    /**
     * Performs messages as part of InMemoryAgentMemory runtime responsibilities.
     * @return The value produced by this operation.
     */
    @Override
    public List<Message> messages()
    {
        return Collections.unmodifiableList(messages);
    }

    /**
     * Adds data to internal state consumed by later runtime steps.
     * @param message The message used by this operation.
     */
    @Override
    public void append(Message message)
    {
        messages.add(message);
    }
}
