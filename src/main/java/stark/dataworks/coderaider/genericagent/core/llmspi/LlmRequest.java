package stark.dataworks.coderaider.genericagent.core.llmspi;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import stark.dataworks.coderaider.genericagent.core.context.ContextItem;
import stark.dataworks.coderaider.genericagent.core.multimodal.GeneratedAsset;
import stark.dataworks.coderaider.genericagent.core.tool.ToolDefinition;

/**
 * LlmRequest implements provider-agnostic model invocation contracts.
 */
@Getter
public class LlmRequest
{

    /**
     * Model id passed to the provider for inference.
     */
    private final String model;

    /**
     * Conversation messages persisted in this session/request.
     */
    private final List<ContextItem> messages;

    /**
     * Registered tools keyed by tool name.
     */
    private final List<ToolDefinition> tools;

    /**
     * Provider/server options forwarded without transformation.
     */
    private final LlmOptions options;

    /**
     * Attachments.
     */
    private final List<GeneratedAsset> attachments;

    /**
     * Initializes LlmRequest with required runtime dependencies and options.
     *
     * @param model    model identifier.
     * @param messages conversation messages.
     * @param tools    tools.
     * @param options  provider options.
     */
    public LlmRequest(String model, List<ContextItem> messages, List<ToolDefinition> tools, LlmOptions options)
    {
        this(model, messages, tools, options, List.of());
    }

    /**
     * Creates an LLM request.
     *
     * @param model       model identifier.
     * @param messages    conversation messages.
     * @param tools       tools.
     * @param options     options map.
     * @param attachments attachments.
     */
    public LlmRequest(String model, List<ContextItem> messages, List<ToolDefinition> tools, LlmOptions options,
                      List<GeneratedAsset> attachments)
    {
        this.model = Objects.requireNonNull(model, "model");
        this.messages = Collections.unmodifiableList(Objects.requireNonNull(messages, "messages"));
        this.tools = Collections.unmodifiableList(Objects.requireNonNull(tools, "tools"));
        this.options = Objects.requireNonNull(options, "options");
        this.attachments = Collections.unmodifiableList(Objects.requireNonNull(attachments, "attachments"));
    }
}
