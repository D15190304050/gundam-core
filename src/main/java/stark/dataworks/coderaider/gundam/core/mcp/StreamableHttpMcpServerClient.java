package stark.dataworks.coderaider.gundam.core.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP-based MCP server client that connects to real MCP servers using Streamable HTTP transport.
 * <p>
 * This client implements the MCP (Model Context Protocol) JSON-RPC 2.0 protocol
 * over HTTP, supporting tool listing and invocation.
 * <p>
 * Usage:
 * <pre>
 * StreamableHttpMcpServerClient client = new StreamableHttpMcpServerClient();
 * McpServerConfiguration config = new McpServerConfiguration("my-server", "http://localhost:9000/mcp", Map.of());
 * List<McpToolDescriptor> tools = client.listTools(config);
 * String result = client.callTool(config, "add", Map.of("a", 1, "b", 2));
 * </pre>
 */
public class StreamableHttpMcpServerClient implements IMcpServerClient
{
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration timeout;
    private final Map<String, String> sessionIds;

    public StreamableHttpMcpServerClient()
    {
        this(Duration.ofSeconds(30));
    }

    public StreamableHttpMcpServerClient(Duration timeout)
    {
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(timeout)
            .build();
        this.objectMapper = new ObjectMapper();
        this.sessionIds = new HashMap<>();
    }

    @Override
    public List<McpToolDescriptor> listTools(McpServerConfiguration config)
    {
        try
        {
            ensureInitialized(config);
            
            ObjectNode request = createRequest("tools/list");
            JsonNode response = sendRequest(config, request);
            
            List<McpToolDescriptor> tools = new ArrayList<>();
            JsonNode toolsNode = response.path("result").path("tools");
            if (toolsNode.isArray())
            {
                for (JsonNode toolNode : toolsNode)
                {
                    String name = toolNode.path("name").asText();
                    String description = toolNode.path("description").asText("");
                    JsonNode inputSchemaNode = toolNode.path("inputSchema");
                    Map<String, Object> inputSchema = new HashMap<>();
                    if (inputSchemaNode.isObject())
                    {
                        inputSchema = objectMapper.convertValue(inputSchemaNode, Map.class);
                    }
                    tools.add(new McpToolDescriptor(name, description, inputSchema));
                }
            }
            return tools;
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Failed to list tools from MCP server: " + config.getServerId(), ex);
        }
    }

    @Override
    public String callTool(McpServerConfiguration config, String toolName, Map<String, Object> args)
    {
        try
        {
            ensureInitialized(config);
            
            ObjectNode request = createRequest("tools/call");
            ObjectNode params = request.putObject("params");
            params.put("name", toolName);
            if (args != null && !args.isEmpty())
            {
                ObjectNode argsNode = params.putObject("arguments");
                args.forEach((key, value) -> {
                    if (value instanceof String)
                    {
                        argsNode.put(key, (String) value);
                    }
                    else if (value instanceof Integer)
                    {
                        argsNode.put(key, (Integer) value);
                    }
                    else if (value instanceof Long)
                    {
                        argsNode.put(key, (Long) value);
                    }
                    else if (value instanceof Double)
                    {
                        argsNode.put(key, (Double) value);
                    }
                    else if (value instanceof Boolean)
                    {
                        argsNode.put(key, (Boolean) value);
                    }
                    else
                    {
                        argsNode.set(key, objectMapper.valueToTree(value));
                    }
                });
            }
            
            JsonNode response = sendRequest(config, request);
            JsonNode content = response.path("result").path("content");
            if (content.isArray() && content.size() > 0)
            {
                StringBuilder result = new StringBuilder();
                for (JsonNode item : content)
                {
                    String type = item.path("type").asText();
                    if ("text".equals(type))
                    {
                        result.append(item.path("text").asText());
                    }
                }
                return result.toString();
            }
            return response.path("result").toString();
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Failed to call tool '" + toolName + "' on MCP server: " + config.getServerId(), ex);
        }
    }

    @Override
    public List<McpResource> listResources(McpServerConfiguration config)
    {
        try
        {
            ensureInitialized(config);
            
            ObjectNode request = createRequest("resources/list");
            JsonNode response = sendRequest(config, request);
            
            List<McpResource> resources = new ArrayList<>();
            JsonNode resourcesNode = response.path("result").path("resources");
            if (resourcesNode.isArray())
            {
                for (JsonNode resourceNode : resourcesNode)
                {
                    String uri = resourceNode.path("uri").asText();
                    String mimeType = resourceNode.path("mimeType").asText("text/plain");
                    resources.add(new McpResource(uri, mimeType, ""));
                }
            }
            return resources;
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Failed to list resources from MCP server: " + config.getServerId(), ex);
        }
    }

    @Override
    public List<McpResourceTemplate> listResourceTemplates(McpServerConfiguration config)
    {
        return List.of();
    }

    @Override
    public McpResource readResource(McpServerConfiguration config, String uri)
    {
        try
        {
            ensureInitialized(config);
            
            ObjectNode request = createRequest("resources/read");
            ObjectNode params = request.putObject("params");
            params.put("uri", uri);
            
            JsonNode response = sendRequest(config, request);
            JsonNode contents = response.path("result").path("contents");
            if (contents.isArray() && contents.size() > 0)
            {
                JsonNode first = contents.get(0);
                String content = first.path("text").asText("");
                String mimeType = first.path("mimeType").asText("text/plain");
                return new McpResource(uri, mimeType, content);
            }
            return new McpResource(uri, "text/plain", "");
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Failed to read resource from MCP server: " + config.getServerId(), ex);
        }
    }

    private void ensureInitialized(McpServerConfiguration config)
    {
        if (sessionIds.containsKey(config.getServerId()))
        {
            return;
        }
        
        try
        {
            String newSessionId = UUID.randomUUID().toString();
            sessionIds.put(config.getServerId(), newSessionId);
            
            ObjectNode initRequest = createRequest("initialize");
            ObjectNode params = initRequest.putObject("params");
            ObjectNode clientInfo = params.putObject("clientInfo");
            clientInfo.put("name", "gundam-core");
            clientInfo.put("version", "1.0.0");
            params.put("protocolVersion", "2024-11-05");
            
            JsonNode response = sendRequest(config, initRequest);
            String serverSessionId = response.path("result").path("sessionId").asText("");
            if (!serverSessionId.isBlank())
            {
                sessionIds.put(config.getServerId(), serverSessionId);
            }
            
            ObjectNode initializedRequest = createRequest("notifications/initialized");
            sendNotification(config, initializedRequest);
        }
        catch (Exception ex)
        {
            sessionIds.remove(config.getServerId());
            throw new RuntimeException("Failed to initialize MCP server: " + config.getServerId(), ex);
        }
    }

    private ObjectNode createRequest(String method)
    {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", UUID.randomUUID().toString());
        request.put("method", method);
        return request;
    }

    private JsonNode sendRequest(McpServerConfiguration config, ObjectNode request) throws Exception
    {
        String body = objectMapper.writeValueAsString(request);
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(config.getEndpoint()))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json, text/event-stream")
            .timeout(timeout)
            .POST(HttpRequest.BodyPublishers.ofString(body));
        
        String sessionId = sessionIds.get(config.getServerId());
        if (sessionId != null && !sessionId.isBlank())
        {
            builder.header("mcp-session-id", sessionId);
        }
        
        Map<String, Object> options = config.getOptions();
        if (options.containsKey("headers") && options.get("headers") instanceof Map)
        {
            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) options.get("headers");
            headers.forEach(builder::header);
        }
        
        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 400)
        {
            throw new RuntimeException("MCP server returned status " + response.statusCode() + ": " + response.body());
        }
        
        String responseBody = response.body();
        String newSessionId = response.headers().firstValue("mcp-session-id").orElse("");
        if (!newSessionId.isBlank())
        {
            sessionIds.put(config.getServerId(), newSessionId);
        }
        
        if (responseBody.startsWith("data:") || responseBody.startsWith("event:"))
        {
            return parseSseResponse(responseBody);
        }
        
        return objectMapper.readTree(responseBody);
    }

    private void sendNotification(McpServerConfiguration config, ObjectNode notification) throws Exception
    {
        String body = objectMapper.writeValueAsString(notification);
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(config.getEndpoint()))
            .header("Content-Type", "application/json")
            .timeout(timeout)
            .POST(HttpRequest.BodyPublishers.ofString(body));
        
        String sessionId = sessionIds.get(config.getServerId());
        if (sessionId != null && !sessionId.isBlank())
        {
            builder.header("mcp-session-id", sessionId);
        }
        
        httpClient.send(builder.build(), HttpResponse.BodyHandlers.discarding());
    }

    private JsonNode parseSseResponse(String sseBody) throws Exception
    {
        String[] lines = sseBody.split("\n");
        for (String line : lines)
        {
            line = line.trim();
            if (line.startsWith("data:"))
            {
                String data = line.substring("data:".length()).trim();
                if (!data.isBlank() && !"[DONE]".equals(data))
                {
                    return objectMapper.readTree(data);
                }
            }
        }
        return objectMapper.createObjectNode();
    }
}
