package stark.dataworks.coderaider.gundam.core.tool.builtin.mcp;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.mcp.McpManager;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
/**
 * Class HostedMcpTool.
 */

public class HostedMcpTool implements ITool
{
    /**
     * Field serverId.
     */
    private final String serverId;
    /**
     * Field toolName.
     */
    private final String toolName;
    /**
     * Field manager.
     */
    private final McpManager manager;
    /**
     * Creates a new HostedMcpTool instance.
     */

    public HostedMcpTool(String serverId, String toolName, McpManager manager)
    {
        this.serverId = serverId;
        this.toolName = toolName;
        this.manager = manager;
    }

    /**
     * Executes definition.
     */
    @Override
    public ToolDefinition definition()
    {
        return new ToolDefinition(toolName, "Hosted MCP proxy tool", java.util.List.of());
    }

    /**
     * Executes execute.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        return manager.resolveToolsAsLocalTools(serverId).stream()
            .filter(t -> t.definition().getName().equals(toolName))
            .findFirst()
            .map(t -> t.execute(input))
            .orElse("Hosted MCP tool not found: " + toolName);
    }
}
