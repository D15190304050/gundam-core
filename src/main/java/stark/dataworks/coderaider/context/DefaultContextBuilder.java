package stark.dataworks.coderaider.context;

import java.util.ArrayList;
import java.util.List;
import stark.dataworks.coderaider.agent.IAgent;
import stark.dataworks.coderaider.memory.IAgentMemory;
import stark.dataworks.coderaider.model.Message;
import stark.dataworks.coderaider.model.Role;

public class DefaultContextBuilder implements IContextBuilder {
    @Override
    public List<Message> build(IAgent agent, IAgentMemory memory, String userInput) {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message(Role.SYSTEM, agent.definition().getSystemPrompt()));
        messages.addAll(memory.messages());
        if (userInput != null && !userInput.isBlank()) {
            messages.add(new Message(Role.USER, userInput));
        }
        return messages;
    }
}
