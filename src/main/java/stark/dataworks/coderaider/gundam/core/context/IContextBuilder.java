package stark.dataworks.coderaider.gundam.core.context;

import java.util.List;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.model.Message;

/**
 * IContextBuilder implements prompt/context assembly before model calls.
 */
public interface IContextBuilder
{

    /**
     * Builds and returns the requested value.
     * @param agent agent instance.
     * @param memory conversation memory backend.
     * @param userInput user input.
     * @return List of message values.
     */
    List<Message> build(IAgent agent, IAgentMemory memory, String userInput);
}
