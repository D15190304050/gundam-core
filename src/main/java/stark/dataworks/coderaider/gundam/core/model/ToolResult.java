package stark.dataworks.coderaider.gundam.core.model;

import lombok.Getter;

import java.util.Objects;

/**
 * ToolResult implements core runtime responsibilities.
 */
@Getter
public class ToolResult
{

    /**
     * Name of the tool being requested or executed.
     */
    private final String toolName;

    /**
     * Main assistant text content returned by the model.
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
