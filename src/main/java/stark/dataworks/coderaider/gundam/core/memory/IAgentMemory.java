package stark.dataworks.coderaider.gundam.core.memory;

import java.util.List;
import stark.dataworks.coderaider.gundam.core.model.Message;

public interface IAgentMemory {
    List<Message> messages();

    void append(Message message);
}
