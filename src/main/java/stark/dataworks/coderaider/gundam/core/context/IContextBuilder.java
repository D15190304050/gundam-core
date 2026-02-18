package stark.dataworks.coderaider.gundam.core.context;

import java.util.List;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.model.Message;

/**
 * IContextBuilder implements prompt/context assembly before model calls.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface IContextBuilder
{

    /**
     * Performs build as part of IContextBuilder runtime responsibilities.
     * @param agent The agent used by this operation.
     * @param memory The memory used by this operation.
     * @param userInput The user input used by this operation.
     * @return The value produced by this operation.
     */
    List<Message> build(IAgent agent, IAgentMemory memory, String userInput);
}
