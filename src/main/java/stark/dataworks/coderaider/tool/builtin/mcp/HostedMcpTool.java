package stark.dataworks.coderaider.tool.builtin.mcp;

import java.util.Map;
import stark.dataworks.coderaider.mcp.McpManager;
import stark.dataworks.coderaider.tool.ITool;
import stark.dataworks.coderaider.tool.ToolDefinition;

public class HostedMcpTool implements ITool {
    private final String serverId;
    private final String toolName;
    private final McpManager manager;

    public HostedMcpTool(String serverId, String toolName, McpManager manager) {
        this.serverId = serverId;
        this.toolName = toolName;
        this.manager = manager;
    }

    @Override
    public ToolDefinition definition() {
        return new ToolDefinition(toolName, "Hosted MCP proxy tool", java.util.List.of());
    }

    @Override
    public String execute(Map<String, Object> input) {
        return manager.resolveToolsAsLocalTools(serverId).stream()
            .filter(t -> t.definition().getName().equals(toolName))
            .findFirst()
            .map(t -> t.execute(input))
            .orElse("Hosted MCP tool not found: " + toolName);
    }
}
