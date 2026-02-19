package stark.dataworks.coderaider.gundam.core.llmspi;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.multimodal.GeneratedAsset;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * LlmRequest implements provider-agnostic model invocation contracts.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
@Getter
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
     * Internal state for attachments; used while coordinating runtime behavior.
     */
    private final List<GeneratedAsset> attachments;

    /**
     * Performs llm request as part of LlmRequest runtime responsibilities.
     * @param model The model used by this operation.
     * @param messages The messages used by this operation.
     * @param tools The tools used by this operation.
     * @param options The options used by this operation.
     */
    public LlmRequest(String model, List<Message> messages, List<ToolDefinition> tools, LlmOptions options)
    {
        this(model, messages, tools, options, List.of());
    }

    /**
     * Performs llm request as part of LlmRequest runtime responsibilities.
     * @param model The model used by this operation.
     * @param messages The messages used by this operation.
     * @param tools The tools used by this operation.
     * @param options The options used by this operation.
     * @param attachments The attachments used by this operation.
     */
    public LlmRequest(String model, List<Message> messages, List<ToolDefinition> tools, LlmOptions options,
                      List<GeneratedAsset> attachments)
    {
        this.model = Objects.requireNonNull(model, "model");
        this.messages = Collections.unmodifiableList(Objects.requireNonNull(messages, "messages"));
        this.tools = Collections.unmodifiableList(Objects.requireNonNull(tools, "tools"));
        this.options = Objects.requireNonNull(options, "options");
        this.attachments = Collections.unmodifiableList(Objects.requireNonNull(attachments, "attachments"));
    }
}
