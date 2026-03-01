package stark.dataworks.coderaider.gundam.core.context;

import java.util.ArrayList;
import java.util.List;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.model.Role;
import stark.dataworks.coderaider.gundam.core.react.ReActPromptComposer;

/**
 * DefaultContextBuilder implements prompt/context assembly before model calls.
 */
public class DefaultContextBuilder implements IContextBuilder
{

    /**
     * Performs build as part of DefaultContextBuilder runtime responsibilities.
     * @param agent The agent used by this operation.
     * @param memory The memory used by this operation.
     * @param userInput The user input used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public List<Message> build(IAgent agent, IAgentMemory memory, String userInput)
    {
        List<Message> messages = new ArrayList<>();
        String systemPrompt = ReActPromptComposer.compose(
            agent.definition().getSystemPrompt(),
            agent.definition().getReactInstructions(),
            agent.definition().isReactEnabled());
        messages.add(new Message(Role.SYSTEM, systemPrompt));
        messages.addAll(memory.messages());
        if (userInput != null && !userInput.isBlank())
        {
            messages.add(new Message(Role.USER, userInput));
        }
        return messages;
    }
}
