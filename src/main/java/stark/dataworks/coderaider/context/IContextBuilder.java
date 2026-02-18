package stark.dataworks.coderaider.context;

import java.util.List;
import stark.dataworks.coderaider.agent.IAgent;
import stark.dataworks.coderaider.memory.IAgentMemory;
import stark.dataworks.coderaider.model.Message;

public interface IContextBuilder {
    List<Message> build(IAgent agent, IAgentMemory memory, String userInput);
}
