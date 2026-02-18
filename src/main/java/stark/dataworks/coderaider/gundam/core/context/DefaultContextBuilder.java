package stark.dataworks.coderaider.gundam.core.context;

import java.util.ArrayList;
import java.util.List;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.model.Role;
/**
 * Class DefaultContextBuilder.
 */

public class DefaultContextBuilder implements IContextBuilder
{
    /**
     * Executes build.
     */
    @Override
    public List<Message> build(IAgent agent, IAgentMemory memory, String userInput)
    {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message(Role.SYSTEM, agent.definition().getSystemPrompt()));
        messages.addAll(memory.messages());
        if (userInput != null && !userInput.isBlank())
        {
            messages.add(new Message(Role.USER, userInput));
        }
        return messages;
    }
}
