package stark.dataworks.coderaider.genericagent.core.context;

import java.util.ArrayList;
import java.util.List;

import stark.dataworks.coderaider.genericagent.core.agent.IAgent;
import stark.dataworks.coderaider.genericagent.core.memory.IAgentMemory;
import stark.dataworks.coderaider.genericagent.core.model.Role;
import stark.dataworks.coderaider.genericagent.core.react.ReActPromptComposer;

/**
 * DefaultContextBuilder implements prompt/context assembly before model calls.
 */
public class DefaultContextBuilder implements IContextBuilder
{

    /**
     * Builds and returns the requested value.
     *
     * @param agent     agent instance.
     * @param memory    conversation memory backend.
     * @param userInput user input.
     * @return List of message values.
     */
    @Override
    public List<ContextItem> build(IAgent agent, IAgentMemory memory, String userInput)
    {
        List<ContextItem> messages = new ArrayList<>();
        String systemPrompt = ReActPromptComposer.compose(
            agent.definition().getSystemPrompt(),
            agent.definition().getReactInstructions(),
            agent.definition().isReactEnabled());
        messages.add(new ContextItem(Role.SYSTEM, systemPrompt));
        messages.addAll(memory.messages());
        if (userInput != null && !userInput.isBlank())
        {
            messages.add(new ContextItem(Role.USER, userInput));
        }
        return messages;
    }
}
