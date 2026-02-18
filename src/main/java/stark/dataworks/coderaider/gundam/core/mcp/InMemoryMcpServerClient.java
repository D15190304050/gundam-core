package stark.dataworks.coderaider.gundam.core.mcp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * InMemoryMcpServerClient implements MCP server integration and tool bridging.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class InMemoryMcpServerClient implements McpServerClient
{

    /**
     * Internal state for tools by server used while coordinating runtime behavior.
     */
    private final Map<String, List<McpToolDescriptor>> toolsByServer = new ConcurrentHashMap<>();

    /**
     * Internal state for handlers used while coordinating runtime behavior.
     */
    private final Map<String, Function<Map<String, Object>, String>> handlers = new ConcurrentHashMap<>();

    /**
     * Internal state for resources by server used while coordinating runtime behavior.
     */
    private final Map<String, List<McpResource>> resourcesByServer = new ConcurrentHashMap<>();

    /**
     * Internal state for templates by server used while coordinating runtime behavior.
     */
    private final Map<String, List<McpResourceTemplate>> templatesByServer = new ConcurrentHashMap<>();

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param serverId The server id used by this operation.
     * @param tools The tools used by this operation.
     */
    public void registerTools(String serverId, List<McpToolDescriptor> tools)
    {
        toolsByServer.put(serverId, tools);
    }

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param serverId The server id used by this operation.
     * @param toolName The tool name used by this operation.
     * @param handler The handler used by this operation.
     */
    public void registerHandler(String serverId, String toolName, Function<Map<String, Object>, String> handler)
    {
        handlers.put(serverId + "::" + toolName, handler);
    }

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param serverId The server id used by this operation.
     * @param resources The resources used by this operation.
     */
    public void registerResources(String serverId, List<McpResource> resources)
    {
        resourcesByServer.put(serverId, resources);
    }

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param serverId The server id used by this operation.
     * @param templates The templates used by this operation.
     */
    public void registerResourceTemplates(String serverId, List<McpResourceTemplate> templates)
    {
        templatesByServer.put(serverId, templates);
    }

    /**
     * Performs list tools as part of InMemoryMcpServerClient runtime responsibilities.
     * @param config The config used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public List<McpToolDescriptor> listTools(McpServerConfig config)
    {
        return toolsByServer.getOrDefault(config.getServerId(), List.of());
    }

    /**
     * Performs call tool as part of InMemoryMcpServerClient runtime responsibilities.
     * @param config The config used by this operation.
     * @param toolName The tool name used by this operation.
     * @param args The args used by this operation.
     * @return The value produced by this operation.
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
     * Performs list resources as part of InMemoryMcpServerClient runtime responsibilities.
     * @param config The config used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public List<McpResource> listResources(McpServerConfig config)
    {
        return resourcesByServer.getOrDefault(config.getServerId(), List.of());
    }

    /**
     * Performs list resource templates as part of InMemoryMcpServerClient runtime responsibilities.
     * @param config The config used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public List<McpResourceTemplate> listResourceTemplates(McpServerConfig config)
    {
        return templatesByServer.getOrDefault(config.getServerId(), List.of());
    }

    /**
     * Performs read resource as part of InMemoryMcpServerClient runtime responsibilities.
     * @param config The config used by this operation.
     * @param uri The uri used by this operation.
     * @return The value produced by this operation.
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
