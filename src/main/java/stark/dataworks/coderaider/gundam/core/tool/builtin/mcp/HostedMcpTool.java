package stark.dataworks.coderaider.gundam.core.tool.builtin.mcp;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.mcp.McpManager;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * HostedMcpTool implements tool contracts, schema metadata, and executable tool registration.
 */
public class HostedMcpTool implements ITool
{

    /**
     * Unique MCP server identifier used for lookup/routing.
     */
    private final String serverId;

    /**
     * Name of the tool being requested or executed.
     */
    private final String toolName;

    /**
     * Manager used to resolve and proxy hosted MCP tools.
     */
    private final McpManager manager;

    /**
     * Initializes HostedMcpTool with required runtime dependencies and options.
     *
     * @param serverId MCP server identifier.
     * @param toolName tool name.
     * @param manager  manager instance.
     */
    public HostedMcpTool(String serverId, String toolName, McpManager manager)
    {
        this.serverId = serverId;
        this.toolName = toolName;
        this.manager = manager;
    }

    /**
     * Returns definition metadata for this component.
     *
     * @return tool definition result.
     */
    @Override
    public ToolDefinition definition()
    {
        return manager.resolveToolsAsLocalTools(serverId).stream()
            .filter(t -> t.definition().getName().equals(toolName))
            .findFirst()
            .map(t -> t.definition())
            .orElse(new ToolDefinition(toolName, "Hosted MCP proxy tool", java.util.List.of()));
    }

    /**
     * Executes this tool operation and returns the produced output.
     *
     * @param input input payload.
     * @return Tool execution output returned by the MCP server.
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
