package stark.dataworks.coderaider.gundam.core.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * ToolCall implements core runtime responsibilities.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
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
     * Performs tool call as part of ToolCall runtime responsibilities.
     * @param toolName The tool name used by this operation.
     * @param arguments The arguments used by this operation.
     */
    public ToolCall(String toolName, Map<String, Object> arguments)
    {
        this.toolName = Objects.requireNonNull(toolName, "toolName");
        this.arguments = Collections.unmodifiableMap(Objects.requireNonNull(arguments, "arguments"));
    }

    /**
     * Returns the current tool name value maintained by this ToolCall.
     * @return The value produced by this operation.
     */
    public String getToolName()
    {
        return toolName;
    }

    /**
     * Returns the current arguments value maintained by this ToolCall.
     * @return The value produced by this operation.
     */
    public Map<String, Object> getArguments()
    {
        return arguments;
    }
}
