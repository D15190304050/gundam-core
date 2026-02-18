package stark.dataworks.coderaider.gundam.core.tool.builtin.mcp;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.mcp.McpManager;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * HostedMcpTool implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class HostedMcpTool implements ITool
{

    /**
     * Internal state for server id; used while coordinating runtime behavior.
     */
    private final String serverId;

    /**
     * Internal state for tool name; used while coordinating runtime behavior.
     */
    private final String toolName;

    /**
     * Internal state for manager; used while coordinating runtime behavior.
     */
    private final McpManager manager;

    /**
     * Performs hosted mcp tool as part of HostedMcpTool runtime responsibilities.
     * @param serverId The server id used by this operation.
     * @param toolName The tool name used by this operation.
     * @param manager The manager used by this operation.
     */
    public HostedMcpTool(String serverId, String toolName, McpManager manager)
    {
        this.serverId = serverId;
        this.toolName = toolName;
        this.manager = manager;
    }

    /**
     * Performs definition as part of HostedMcpTool runtime responsibilities.
     * @return The value produced by this operation.
     */
    @Override
    public ToolDefinition definition()
    {
        return new ToolDefinition(toolName, "Hosted MCP proxy tool", java.util.List.of());
    }

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param input The input used by this operation.
     * @return The value produced by this operation.
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
