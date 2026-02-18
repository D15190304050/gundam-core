package stark.dataworks.coderaider.gundam.core.llmspi;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * LlmRequest implements provider-agnostic model invocation contracts.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class LlmRequest
{

    /**
     * Internal state for model; used while coordinating runtime behavior.
     */
    private final String model;

    /**
     * Internal state for messages; used while coordinating runtime behavior.
     */
    private final List<Message> messages;

    /**
     * Internal state for tools; used while coordinating runtime behavior.
     */
    private final List<ToolDefinition> tools;

    /**
     * Internal state for options; used while coordinating runtime behavior.
     */
    private final LlmOptions options;

    /**
     * Performs llm request as part of LlmRequest runtime responsibilities.
     * @param model The model used by this operation.
     * @param messages The messages used by this operation.
     * @param tools The tools used by this operation.
     * @param options The options used by this operation.
     */
    public LlmRequest(String model, List<Message> messages, List<ToolDefinition> tools, LlmOptions options)
    {
        this.model = Objects.requireNonNull(model, "model");
        this.messages = Collections.unmodifiableList(Objects.requireNonNull(messages, "messages"));
        this.tools = Collections.unmodifiableList(Objects.requireNonNull(tools, "tools"));
        this.options = Objects.requireNonNull(options, "options");
    }

    /**
     * Returns the current model value maintained by this LlmRequest.
     * @return The value produced by this operation.
     */
    public String getModel()
    {
        return model;
    }

    /**
     * Returns the current messages value maintained by this LlmRequest.
     * @return The value produced by this operation.
     */
    public List<Message> getMessages()
    {
        return messages;
    }

    /**
     * Returns the current tools value maintained by this LlmRequest.
     * @return The value produced by this operation.
     */
    public List<ToolDefinition> getTools()
    {
        return tools;
    }

    /**
     * Returns the current options value maintained by this LlmRequest.
     * @return The value produced by this operation.
     */
    public LlmOptions getOptions()
    {
        return options;
    }
}
