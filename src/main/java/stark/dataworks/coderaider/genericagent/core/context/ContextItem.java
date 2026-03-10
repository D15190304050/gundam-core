package stark.dataworks.coderaider.genericagent.core.context;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import stark.dataworks.coderaider.genericagent.core.model.Role;
import stark.dataworks.coderaider.genericagent.core.model.ToolCall;
import stark.dataworks.coderaider.genericagent.core.multimodal.MessagePart;
import stark.dataworks.coderaider.genericagent.core.multimodal.MessagePartType;

/**
 * RunItem implements normalized run result structures.
 */
@Getter
public class ContextItem
{

    /**
     * Type discriminator for this item/event/span.
     */
    private final ContextItemType type;

    /**
     * ContextItem role (system, user, assistant, tool).
     */
    private final Role role;

    /**
     * Main assistant text content returned by the model.
     */
    private final String content;

    /**
     * Multimodal message parts composing this item.
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
     * Arbitrary metadata attached for caller-specific routing or auditing.
     */
    private final Map<String, Object> metadata;

    /**
     * Initializes ContextItem with required runtime dependencies and options.
     *
     * @param type     type discriminator.
     * @param content  content.
     * @param metadata metadata map.
     */
    public ContextItem(ContextItemType type, String content, Map<String, Object> metadata)
    {
        this.type = Objects.requireNonNull(type, "type");
        this.role = roleFromType(type);
        this.parts = List.of(MessagePart.text(content == null ? "" : content));
        this.content = this.parts.stream()
            .filter(p -> p.getType() == MessagePartType.TEXT)
            .map(MessagePart::getText)
            .collect(Collectors.joining());
        this.toolCallId = null;
        this.toolCalls = List.of();
        this.metadata = Collections.unmodifiableMap(metadata == null ? Map.of() : metadata);
    }

    /**
     * Initializes ContextItem with required runtime dependencies and options.
     *
     * @param role    role.
     * @param content content.
     */
    public ContextItem(Role role, String content)
    {
        this(role, List.of(MessagePart.text(content)), null, null);
    }

    /**
     * Initializes ContextItem with required runtime dependencies and options.
     *
     * @param role  role.
     * @param parts parts.
     */
    public ContextItem(Role role, List<MessagePart> parts)
    {
        this(role, parts, null, null);
    }

    /**
     * Initializes ContextItem with required runtime dependencies and options.
     *
     * @param role       role.
     * @param content    content.
     * @param toolCallId tool call identifier.
     */
    public ContextItem(Role role, String content, String toolCallId)
    {
        this(role, List.of(MessagePart.text(content)), toolCallId, null);
    }

    /**
     * Initializes ContextItem with required runtime dependencies and options.
     *
     * @param role      role.
     * @param content   content.
     * @param toolCalls tool calls.
     */
    public ContextItem(Role role, String content, List<ToolCall> toolCalls)
    {
        this(role, List.of(MessagePart.text(content)), null, toolCalls);
    }

    /**
     * Initializes ContextItem with required runtime dependencies and options.
     *
     * @param role       role.
     * @param parts      parts.
     * @param toolCallId tool call identifier.
     * @param toolCalls  tool calls.
     */
    public ContextItem(Role role, List<MessagePart> parts, String toolCallId, List<ToolCall> toolCalls)
    {
        this.type = typeFromRole(role);
        this.role = Objects.requireNonNull(role, "role");
        this.parts = Collections.unmodifiableList(Objects.requireNonNull(parts, "parts"));
        this.content = this.parts.stream()
            .filter(p -> p.getType() == MessagePartType.TEXT)
            .map(MessagePart::getText)
            .collect(Collectors.joining());
        this.toolCallId = toolCallId;
        this.toolCalls = toolCalls != null ? Collections.unmodifiableList(toolCalls) : Collections.emptyList();
        this.metadata = Map.of();
    }

    private static Role roleFromType(ContextItemType type)
    {
        return switch (type)
        {
            case USER_MESSAGE -> Role.USER;
            case ASSISTANT_MESSAGE -> Role.ASSISTANT;
            case TOOL_RESULT -> Role.TOOL;
            case SYSTEM_EVENT -> Role.SYSTEM;
            default -> null;
        };
    }

    private static ContextItemType typeFromRole(Role role)
    {
        Objects.requireNonNull(role, "role");
        return switch (role)
        {
            case USER -> ContextItemType.USER_MESSAGE;
            case ASSISTANT -> ContextItemType.ASSISTANT_MESSAGE;
            case TOOL -> ContextItemType.TOOL_RESULT;
            case SYSTEM -> ContextItemType.SYSTEM_EVENT;
        };
    }
}
