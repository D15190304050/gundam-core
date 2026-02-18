package stark.dataworks.coderaider.gundam.core.mcp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
/**
 * Class InMemoryMcpServerClient.
 */

public class InMemoryMcpServerClient implements McpServerClient
{
    /**
     * Field toolsByServer.
     */
    private final Map<String, List<McpToolDescriptor>> toolsByServer = new ConcurrentHashMap<>();
    /**
     * Field handlers.
     */
    private final Map<String, Function<Map<String, Object>, String>> handlers = new ConcurrentHashMap<>();
    /**
     * Field resourcesByServer.
     */
    private final Map<String, List<McpResource>> resourcesByServer = new ConcurrentHashMap<>();
    /**
     * Field templatesByServer.
     */
    private final Map<String, List<McpResourceTemplate>> templatesByServer = new ConcurrentHashMap<>();
    /**
     * Executes registerTools.
     */

    public void registerTools(String serverId, List<McpToolDescriptor> tools)
    {
        toolsByServer.put(serverId, tools);
    }
    /**
     * Executes registerHandler.
     */

    public void registerHandler(String serverId, String toolName, Function<Map<String, Object>, String> handler)
    {
        handlers.put(serverId + "::" + toolName, handler);
    }
    /**
     * Executes registerResources.
     */

    public void registerResources(String serverId, List<McpResource> resources)
    {
        resourcesByServer.put(serverId, resources);
    }
    /**
     * Executes registerResourceTemplates.
     */

    public void registerResourceTemplates(String serverId, List<McpResourceTemplate> templates)
    {
        templatesByServer.put(serverId, templates);
    }

    /**
     * Executes listTools.
     */
    @Override
    public List<McpToolDescriptor> listTools(McpServerConfig config)
    {
        return toolsByServer.getOrDefault(config.getServerId(), List.of());
    }

    /**
     * Executes callTool.
     */
    @Override
    public String callTool(McpServerConfig config, String toolName, Map<String, Object> args)
    {
        Function<Map<String, Object>, String> fn = handlers.get(config.getServerId() + "::" + toolName);
        if (fn == null)
        {
            return "MCP tool handler missing: " + toolName;
        }
        return fn.apply(args);
    }

    /**
     * Executes listResources.
     */
    @Override
    public List<McpResource> listResources(McpServerConfig config)
    {
        return resourcesByServer.getOrDefault(config.getServerId(), List.of());
    }

    /**
     * Executes listResourceTemplates.
     */
    @Override
    public List<McpResourceTemplate> listResourceTemplates(McpServerConfig config)
    {
        return templatesByServer.getOrDefault(config.getServerId(), List.of());
    }

    /**
     * Executes readResource.
     */
    @Override
    public McpResource readResource(McpServerConfig config, String uri)
    {
        return listResources(config).stream()
            .filter(r -> r.uri().equals(uri))
            .findFirst()
            .orElse(new McpResource(uri, "text/plain", ""));
    }
}
