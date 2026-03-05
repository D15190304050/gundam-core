package stark.dataworks.coderaider.gundam.core.model;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import stark.dataworks.coderaider.gundam.core.multimodal.MessagePart;
import stark.dataworks.coderaider.gundam.core.multimodal.MessagePartType;

/**
 * Message implements core runtime responsibilities.
 */
@Getter
public class Message
{

    /**
     * Message role (system, user, assistant, tool).
     */
    private final Role role;

    /**
     * Main assistant text content returned by the model.
     */
    private final String content;

    /**
     * Multimodal message parts composing this message.
     */
    private final List<MessagePart> parts;

    /**
     * Identifier used to correlate a tool message with the originating tool call.
     */
    private final String toolCallId;

    /**
     * Tool call payload emitted by assistant messages when function execution is requested.
     */
    private final List<ToolCall> toolCalls;

    /**
     * Initializes Message with required runtime dependencies and options.
     * @param role role.
     * @param content content.
     */
    public Message(Role role, String content)
    {
        this(role, List.of(MessagePart.text(content)), null, null);
    }

    /**
     * Initializes Message with required runtime dependencies and options.
     * @param role role.
     * @param parts parts.
     */
    public Message(Role role, List<MessagePart> parts)
    {
        this(role, parts, null, null);
    }

    /**
     * Initializes Message with required runtime dependencies and options.
     * @param role role.
     * @param content content.
     * @param toolCallId tool call identifier.
     */
    public Message(Role role, String content, String toolCallId)
    {
        this(role, List.of(MessagePart.text(content)), toolCallId, null);
    }

    /**
     * Initializes Message with required runtime dependencies and options.
     * @param role role.
     * @param content content.
     * @param toolCalls tool calls.
     */
    public Message(Role role, String content, List<ToolCall> toolCalls)
    {
        this(role, List.of(MessagePart.text(content)), null, toolCalls);
    }

    /**
     * Initializes Message with required runtime dependencies and options.
     * @param role role.
     * @param parts parts.
     * @param toolCallId tool call identifier.
     * @param toolCalls tool calls.
     */
    public Message(Role role, List<MessagePart> parts, String toolCallId, List<ToolCall> toolCalls)
    {
        this.role = Objects.requireNonNull(role, "role");
        this.parts = Collections.unmodifiableList(Objects.requireNonNull(parts, "parts"));
        this.content = this.parts.stream()
            .filter(p -> p.getType() == MessagePartType.TEXT)
            .map(MessagePart::getText)
            .collect(Collectors.joining());
        this.toolCallId = toolCallId;
        this.toolCalls = toolCalls != null ? Collections.unmodifiableList(toolCalls) : Collections.emptyList();
    }
}
