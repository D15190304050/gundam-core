package stark.dataworks.coderaider.gundam.core.llmspi;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
/**
 * Class LlmRequest.
 */

public class LlmRequest
{
    /**
     * Field model.
     */
    private final String model;
    /**
     * Field messages.
     */
    private final List<Message> messages;
    /**
     * Field tools.
     */
    private final List<ToolDefinition> tools;
    /**
     * Field options.
     */
    private final LlmOptions options;
    /**
     * Creates a new LlmRequest instance.
     */

    public LlmRequest(String model, List<Message> messages, List<ToolDefinition> tools, LlmOptions options)
    {
        this.model = Objects.requireNonNull(model, "model");
        this.messages = Collections.unmodifiableList(Objects.requireNonNull(messages, "messages"));
        this.tools = Collections.unmodifiableList(Objects.requireNonNull(tools, "tools"));
        this.options = Objects.requireNonNull(options, "options");
    }
    /**
     * Executes getModel.
     */

    public String getModel()
    {
        return model;
    }
    /**
     * Executes getMessages.
     */

    public List<Message> getMessages()
    {
        return messages;
    }
    /**
     * Executes getTools.
     */

    public List<ToolDefinition> getTools()
    {
        return tools;
    }
    /**
     * Executes getOptions.
     */

    public LlmOptions getOptions()
    {
        return options;
    }
}
