package stark.dataworks.coderaider.genericagent.core.context;

import java.util.List;

import stark.dataworks.coderaider.genericagent.core.agent.IAgent;
import stark.dataworks.coderaider.genericagent.core.memory.IAgentMemory;

/**
 * IContextBuilder implements prompt/context assembly before model calls.
 */
public interface IContextBuilder
{

    /**
     * Builds and returns the requested value.
     *
     * @param agent     agent instance.
     * @param memory    conversation memory backend.
     * @param userInput user input.
     * @return List of message values.
     */
    List<ContextItem> build(IAgent agent, IAgentMemory memory, String userInput);
}
