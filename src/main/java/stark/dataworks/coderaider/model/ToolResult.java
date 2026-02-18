package stark.dataworks.coderaider.model;

import java.util.Objects;

public class ToolResult {
    private final String toolName;
    private final String content;

    public ToolResult(String toolName, String content) {
        this.toolName = Objects.requireNonNull(toolName, "toolName");
        this.content = Objects.requireNonNull(content, "content");
    }

    public String getToolName() {
        return toolName;
    }

    public String getContent() {
        return content;
    }
}
