package stark.dataworks.coderaider.mcp;

import java.util.List;
import java.util.Map;

public interface McpServerClient {
    List<McpToolDescriptor> listTools(McpServerConfig config);

    String callTool(McpServerConfig config, String toolName, Map<String, Object> args);

    List<McpResource> listResources(McpServerConfig config);

    List<McpResourceTemplate> listResourceTemplates(McpServerConfig config);

    McpResource readResource(McpServerConfig config, String uri);
}
