package stark.dataworks.coderaider.gundam.core.mcp;

import java.util.List;
import java.util.Map;

/**
 * McpServerClient implements MCP server integration and tool bridging.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface McpServerClient
{

    /**
     * Performs list tools as part of McpServerClient runtime responsibilities.
     * @param config The config used by this operation.
     * @return The value produced by this operation.
     */
    List<McpToolDescriptor> listTools(McpServerConfig config);

    /**
     * Performs call tool as part of McpServerClient runtime responsibilities.
     * @param config The config used by this operation.
     * @param toolName The tool name used by this operation.
     * @param args The args used by this operation.
     * @return The value produced by this operation.
     */

    String callTool(McpServerConfig config, String toolName, Map<String, Object> args);

    /**
     * Performs list resources as part of McpServerClient runtime responsibilities.
     * @param config The config used by this operation.
     * @return The value produced by this operation.
     */

    List<McpResource> listResources(McpServerConfig config);

    /**
     * Performs list resource templates as part of McpServerClient runtime responsibilities.
     * @param config The config used by this operation.
     * @return The value produced by this operation.
     */

    List<McpResourceTemplate> listResourceTemplates(McpServerConfig config);

    /**
     * Performs read resource as part of McpServerClient runtime responsibilities.
     * @param config The config used by this operation.
     * @param uri The uri used by this operation.
     * @return The value produced by this operation.
     */

    McpResource readResource(McpServerConfig config, String uri);
}
