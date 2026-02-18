package stark.dataworks.coderaider.gundam.core.model;

import java.util.Objects;
/**
 * Class ToolResult.
 */

public class ToolResult
{
    /**
     * Field toolName.
     */
    private final String toolName;
    /**
     * Field content.
     */
    private final String content;
    /**
     * Creates a new ToolResult instance.
     */

    public ToolResult(String toolName, String content)
    {
        this.toolName = Objects.requireNonNull(toolName, "toolName");
        this.content = Objects.requireNonNull(content, "content");
    }
    /**
     * Executes getToolName.
     */

    public String getToolName()
    {
        return toolName;
    }
    /**
     * Executes getContent.
     */

    public String getContent()
    {
        return content;
    }
}
