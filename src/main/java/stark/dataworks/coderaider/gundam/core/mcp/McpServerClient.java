package stark.dataworks.coderaider.gundam.core.mcp;

import java.util.List;
import java.util.Map;
/**
 * Interface McpServerClient.
 */

public interface McpServerClient
{
    /**
     * Executes listTools.
     */
    List<McpToolDescriptor> listTools(McpServerConfig config);
    /**
     * Executes callTool.
     */

    String callTool(McpServerConfig config, String toolName, Map<String, Object> args);
    /**
     * Executes listResources.
     */

    List<McpResource> listResources(McpServerConfig config);
    /**
     * Executes listResourceTemplates.
     */

    List<McpResourceTemplate> listResourceTemplates(McpServerConfig config);
    /**
     * Executes readResource.
     */

    McpResource readResource(McpServerConfig config, String uri);
}
