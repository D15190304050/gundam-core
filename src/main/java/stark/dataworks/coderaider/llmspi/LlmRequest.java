package stark.dataworks.coderaider.llmspi;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import stark.dataworks.coderaider.model.Message;
import stark.dataworks.coderaider.tool.ToolDefinition;

public class LlmRequest {
    private final String model;
    private final List<Message> messages;
    private final List<ToolDefinition> tools;
    private final LlmOptions options;

    public LlmRequest(String model, List<Message> messages, List<ToolDefinition> tools, LlmOptions options) {
        this.model = Objects.requireNonNull(model, "model");
        this.messages = Collections.unmodifiableList(Objects.requireNonNull(messages, "messages"));
        this.tools = Collections.unmodifiableList(Objects.requireNonNull(tools, "tools"));
        this.options = Objects.requireNonNull(options, "options");
    }

    public String getModel() {
        return model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<ToolDefinition> getTools() {
        return tools;
    }

    public LlmOptions getOptions() {
        return options;
    }
}
