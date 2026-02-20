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
     * Internal state for role; used while coordinating runtime behavior.
     */
    private final Role role;

    /**
     * Internal state for content; used while coordinating runtime behavior.
     */
    private final String content;

    /**
     * Internal state for parts; used while coordinating runtime behavior.
     */
    private final List<MessagePart> parts;

    /**
     * Internal state for tool call ID; used for tool role messages to correlate with their calls.
     */
    private final String toolCallId;

    /**
     * Internal state for tool calls; used for assistant messages that contain tool calls.
     */
    private final List<ToolCall> toolCalls;

    /**
     * Performs message as part of Message runtime responsibilities.
     * @param role The role used by this operation.
     * @param content The content used by this operation.
     */
    public Message(Role role, String content)
    {
        this(role, List.of(MessagePart.text(content)), null, null);
    }

    /**
     * Performs message as part of Message runtime responsibilities.
     * @param role The role used by this operation.
     * @param parts The parts used by this operation.
     */
    public Message(Role role, List<MessagePart> parts)
    {
        this(role, parts, null, null);
    }

    /**
     * Performs message as part of Message runtime responsibilities.
     * @param role The role used by this operation.
     * @param content The content used by this operation.
     * @param toolCallId The tool call ID used for tool role messages.
     */
    public Message(Role role, String content, String toolCallId)
    {
        this(role, List.of(MessagePart.text(content)), toolCallId, null);
    }

    /**
     * Performs message as part of Message runtime responsibilities.
     * @param role The role used by this operation.
     * @param content The content used by this operation.
     * @param toolCalls The tool calls used for assistant messages.
     */
    public Message(Role role, String content, List<ToolCall> toolCalls)
    {
        this(role, List.of(MessagePart.text(content)), null, toolCalls);
    }

    /**
     * Performs message as part of Message runtime responsibilities.
     * @param role The role used by this operation.
     * @param parts The parts used by this operation.
     * @param toolCallId The tool call ID used for tool role messages.
     * @param toolCalls The tool calls used for assistant messages.
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
