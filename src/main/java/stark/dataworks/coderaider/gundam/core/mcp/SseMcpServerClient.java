package stark.dataworks.coderaider.gundam.core.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SSE-based MCP server client that connects to real MCP servers using Server-Sent Events transport.
 * <p>
 * This client implements the MCP (Model Context Protocol) JSON-RPC 2.0 protocol
 * over SSE, supporting tool listing and invocation.
 * <p>
 * Usage:
 * <pre>
 * SseMcpServerClient client = new SseMcpServerClient();
 * McpServerConfiguration config = new McpServerConfiguration("my-server", "http://localhost:9000/sse", Map.of());
 * client.connect(config);
 * List<McpToolDescriptor> tools = client.listTools(config);
 * String result = client.callTool(config, "add", Map.of("a", 1, "b", 2));
 * </pre>
 */
public class SseMcpServerClient implements IMcpServerClient
{
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration timeout;
    private final Map<String, String> messageEndpoints;
    private final AtomicLong requestIdCounter;

    public SseMcpServerClient()
    {
        this(Duration.ofSeconds(30));
    }

    public SseMcpServerClient(Duration timeout)
    {
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(timeout)
            .build();
        this.objectMapper = new ObjectMapper();
        this.messageEndpoints = new ConcurrentHashMap<>();
        this.requestIdCounter = new AtomicLong(0);
    }

    public void connect(McpServerConfiguration config)
    {
        if (messageEndpoints.containsKey(config.getServerId()))
        {
            return;
        }
        
        try
        {
            String sseEndpoint = config.getEndpoint();
            URI sseUri = URI.create(sseEndpoint);
            String baseUrl = sseUri.getScheme() + "://" + sseUri.getHost() + (sseUri.getPort() > 0 ? ":" + sseUri.getPort() : "");
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(sseUri)
                .header("Accept", "text/event-stream")
                .timeout(timeout)
                .GET()
                .build();
            
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            
            if (response.statusCode() >= 400)
            {
                throw new RuntimeException("Failed to connect to MCP server: status " + response.statusCode());
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if (line.startsWith("event:"))
                    {
                        String eventType = line.substring("event:".length()).trim();
                        line = reader.readLine();
                        if (line != null && line.startsWith("data:"))
                        {
                            String data = line.substring("data:".length()).trim();
                            System.out.println("[DEBUG] SSE event: " + eventType + ", data: " + data);
                            if ("endpoint".equals(eventType))
                            {
                                String messageEndpoint = data;
                                if (!messageEndpoint.startsWith("http"))
                                {
                                    messageEndpoint = baseUrl + messageEndpoint;
                                }
                                System.out.println("[DEBUG] Message endpoint: " + messageEndpoint);
                                messageEndpoints.put(config.getServerId(), messageEndpoint);
                                break;
                            }
                        }
                    }
                }
            }
            
            String messageEndpoint = messageEndpoints.get(config.getServerId());
            if (messageEndpoint == null)
            {
                throw new RuntimeException("Failed to get message endpoint from MCP server");
            }
            
            initialize(config);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Failed to connect to MCP server: " + config.getServerId(), ex);
        }
    }

    private void initialize(McpServerConfiguration config)
    {
        try
        {
            ObjectNode initRequest = createRequest("initialize");
            ObjectNode params = initRequest.putObject("params");
            ObjectNode clientInfo = params.putObject("clientInfo");
            clientInfo.put("name", "gundam-core");
            clientInfo.put("version", "1.0.0");
            params.put("protocolVersion", "2024-11-05");
            
            JsonNode response = sendRequest(config, initRequest);
            
            ObjectNode initializedRequest = createRequest("notifications/initialized");
            sendNotification(config, initializedRequest);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Failed to initialize MCP server: " + config.getServerId(), ex);
        }
    }

    @Override
    public List<McpToolDescriptor> listTools(McpServerConfiguration config)
    {
        try
        {
            ensureConnected(config);
            
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
            ensureConnected(config);
            
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
            ensureConnected(config);
            
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
            ensureConnected(config);
            
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

    private void ensureConnected(McpServerConfiguration config)
    {
        if (!messageEndpoints.containsKey(config.getServerId()))
        {
            connect(config);
        }
    }

    private ObjectNode createRequest(String method)
    {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", String.valueOf(requestIdCounter.incrementAndGet()));
        request.put("method", method);
        return request;
    }

    private JsonNode sendRequest(McpServerConfiguration config, ObjectNode request) throws Exception
    {
        String messageEndpoint = messageEndpoints.get(config.getServerId());
        if (messageEndpoint == null)
        {
            throw new RuntimeException("Not connected to MCP server: " + config.getServerId());
        }
        
        String body = objectMapper.writeValueAsString(request);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(messageEndpoint))
            .header("Content-Type", "application/json")
            .timeout(timeout)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 400)
        {
            throw new RuntimeException("MCP server returned status " + response.statusCode() + ": " + response.body());
        }
        
        String responseBody = response.body();
        return objectMapper.readTree(responseBody);
    }

    private void sendNotification(McpServerConfiguration config, ObjectNode notification) throws Exception
    {
        String messageEndpoint = messageEndpoints.get(config.getServerId());
        if (messageEndpoint == null)
        {
            throw new RuntimeException("Not connected to MCP server: " + config.getServerId());
        }
        
        notification.remove("id");
        String body = objectMapper.writeValueAsString(notification);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(messageEndpoint))
            .header("Content-Type", "application/json")
            .timeout(timeout)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        
        httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
    }
}
