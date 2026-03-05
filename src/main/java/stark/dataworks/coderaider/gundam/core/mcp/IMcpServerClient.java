package stark.dataworks.coderaider.gundam.core.mcp;

import java.util.List;
import java.util.Map;

/**
 * McpServerClient implements MCP server integration and tool bridging.
 */
public interface IMcpServerClient
{

    /**
     * Returns the tools exposed by the target registry or MCP server.
     * @param config MCP server configuration.
     * @return List of mcp tool descriptor values.
     */
    List<McpToolDescriptor> listTools(McpServerConfiguration config);

    /**
     * Calls the specified tool provided by the MCP server.
     * @param config MCP server configuration.
     * @param toolName tool name.
     * @param args tool arguments passed to the MCP server.
     * @return Tool execution output returned by the MCP server.
     */

    String callTool(McpServerConfiguration config, String toolName, Map<String, Object> args);

    /**
     * Returns resources exposed by the target MCP server.
     * @param config MCP server configuration.
     * @return List of mcp resource values.
     */

    List<McpResource> listResources(McpServerConfiguration config);

    /**
     * Returns resource templates exposed by the target MCP server.
     * @param config MCP server configuration.
     * @return List of mcp resource template values.
     */

    List<McpResourceTemplate> listResourceTemplates(McpServerConfiguration config);

    /**
     * Reads the specified resource content from the MCP server.
     * @param config MCP server configuration.
     * @param uri resource URI.
     * @return Resource payload returned by the MCP server.
     */

    McpResource readResource(McpServerConfiguration config, String uri);
}
