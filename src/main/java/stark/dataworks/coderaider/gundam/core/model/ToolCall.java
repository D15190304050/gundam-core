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
     * Name of the tool being requested or executed.
     */
    private final String toolName;

    /**
     * Tool-call arguments provided by the model/caller.
     */
    private final Map<String, Object> arguments;

    /**
     * Identifier used to correlate a tool message with the originating tool call.
     */
    private final String toolCallId;

    /**
     * Initializes ToolCall with required runtime dependencies and options.
     * @param toolName tool name.
     * @param Map<String map<string.
     * @param arguments arguments.
     */
    public ToolCall(String toolName, Map<String, Object> arguments)
    {
        this(toolName, arguments, UUID.randomUUID().toString());
    }

    /**
     * Initializes ToolCall with required runtime dependencies and options.
     * @param toolName tool name.
     * @param Map<String map<string.
     * @param arguments arguments.
     * @param toolCallId tool call identifier.
     */
    public ToolCall(String toolName, Map<String, Object> arguments, String toolCallId)
    {
        this.toolName = Objects.requireNonNull(toolName, "toolName");
        this.arguments = Collections.unmodifiableMap(Objects.requireNonNull(arguments, "arguments"));
        this.toolCallId = toolCallId != null ? toolCallId : UUID.randomUUID().toString();
    }
}
