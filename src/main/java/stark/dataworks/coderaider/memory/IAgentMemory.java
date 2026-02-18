package stark.dataworks.coderaider.memory;

import java.util.List;
import stark.dataworks.coderaider.model.Message;

public interface IAgentMemory {
    List<Message> messages();

    void append(Message message);
}
