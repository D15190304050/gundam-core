package stark.dataworks.coderaider.mcp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class InMemoryMcpServerClient implements McpServerClient {
    private final Map<String, List<McpToolDescriptor>> toolsByServer = new ConcurrentHashMap<>();
    private final Map<String, Function<Map<String, Object>, String>> handlers = new ConcurrentHashMap<>();
    private final Map<String, List<McpResource>> resourcesByServer = new ConcurrentHashMap<>();
    private final Map<String, List<McpResourceTemplate>> templatesByServer = new ConcurrentHashMap<>();

    public void registerTools(String serverId, List<McpToolDescriptor> tools) {
        toolsByServer.put(serverId, tools);
    }

    public void registerHandler(String serverId, String toolName, Function<Map<String, Object>, String> handler) {
        handlers.put(serverId + "::" + toolName, handler);
    }

    public void registerResources(String serverId, List<McpResource> resources) {
        resourcesByServer.put(serverId, resources);
    }

    public void registerResourceTemplates(String serverId, List<McpResourceTemplate> templates) {
        templatesByServer.put(serverId, templates);
    }

    @Override
    public List<McpToolDescriptor> listTools(McpServerConfig config) {
        return toolsByServer.getOrDefault(config.getServerId(), List.of());
    }

    @Override
    public String callTool(McpServerConfig config, String toolName, Map<String, Object> args) {
        Function<Map<String, Object>, String> fn = handlers.get(config.getServerId() + "::" + toolName);
        if (fn == null) {
            return "MCP tool handler missing: " + toolName;
        }
        return fn.apply(args);
    }

    @Override
    public List<McpResource> listResources(McpServerConfig config) {
        return resourcesByServer.getOrDefault(config.getServerId(), List.of());
    }

    @Override
    public List<McpResourceTemplate> listResourceTemplates(McpServerConfig config) {
        return templatesByServer.getOrDefault(config.getServerId(), List.of());
    }

    @Override
    public McpResource readResource(McpServerConfig config, String uri) {
        return listResources(config).stream()
            .filter(r -> r.uri().equals(uri))
            .findFirst()
            .orElse(new McpResource(uri, "text/plain", ""));
    }
}
