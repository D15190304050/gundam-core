package stark.dataworks.coderaider.gundam.core.model;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * ToolCall implements core runtime responsibilities.
 */
@Getter
public class ToolCall
{

    /**
     * Internal state for tool name; used while coordinating runtime behavior.
     */
    private final String toolName;

    /**
     * Internal state for arguments; used while coordinating runtime behavior.
     */
    private final Map<String, Object> arguments;

    /**
     * Internal state for tool call ID; used to correlate tool results with their calls.
     */
    private final String toolCallId;

    /**
     * Performs tool call as part of ToolCall runtime responsibilities.
     * @param toolName The tool name used by this operation.
     * @param arguments The arguments used by this operation.
     */
    public ToolCall(String toolName, Map<String, Object> arguments)
    {
        this(toolName, arguments, UUID.randomUUID().toString());
    }

    /**
     * Performs tool call as part of ToolCall runtime responsibilities.
     * @param toolName The tool name used by this operation.
     * @param arguments The arguments used by this operation.
     * @param toolCallId The tool call ID used by this operation.
     */
    public ToolCall(String toolName, Map<String, Object> arguments, String toolCallId)
    {
        this.toolName = Objects.requireNonNull(toolName, "toolName");
        this.arguments = Collections.unmodifiableMap(Objects.requireNonNull(arguments, "arguments"));
        this.toolCallId = toolCallId != null ? toolCallId : UUID.randomUUID().toString();
    }
}
