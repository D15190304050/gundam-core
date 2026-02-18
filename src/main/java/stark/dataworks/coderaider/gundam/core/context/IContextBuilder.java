package stark.dataworks.coderaider.gundam.core.context;

import java.util.List;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.model.Message;
/**
 * Interface IContextBuilder.
 */

public interface IContextBuilder
{
    /**
     * Executes build.
     */
    List<Message> build(IAgent agent, IAgentMemory memory, String userInput);
}
