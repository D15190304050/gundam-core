package stark.dataworks.coderaider.gundam.core.model;

import lombok.Getter;

import java.util.Objects;

/**
 * ToolResult implements core runtime responsibilities.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
@Getter
public class ToolResult
{

    /**
     * Internal state for tool name; used while coordinating runtime behavior.
     */
    private final String toolName;

    /**
     * Internal state for content; used while coordinating runtime behavior.
     */
    private final String content;

    /**
     * Performs tool result as part of ToolResult runtime responsibilities.
     * @param toolName The tool name used by this operation.
     * @param content The content used by this operation.
     */
    public ToolResult(String toolName, String content)
    {
        this.toolName = Objects.requireNonNull(toolName, "toolName");
        this.content = Objects.requireNonNull(content, "content");
    }
}
