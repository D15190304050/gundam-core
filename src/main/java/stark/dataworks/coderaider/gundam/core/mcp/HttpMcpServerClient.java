package stark.dataworks.coderaider.gundam.core.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HTTP-based MCP server client that connects to MCP servers using SSE transport.
 * <p>
 * This client implements the MCP (Model Context Protocol) JSON-RPC 2.0 protocol
 * over HTTP with SSE, supporting tool listing and invocation.
 * <p>
 * Usage:
 * <pre>
 * HttpMcpServerClient client = new HttpMcpServerClient();
 * McpServerConfiguration config = new McpServerConfiguration("my-server", "http://localhost:8765", Map.of());
 * client.connect(config);
 * List<McpToolDescriptor> tools = client.listTools(config);
 * String result = client.callTool(config, "add", Map.of("a", 1, "b", 2));
 * </pre>
 */
public class HttpMcpServerClient implements IMcpServerClient
{
    private final ObjectMapper objectMapper;
    private final Map<String, String> sessionIds;
    private final Map<String, Boolean> connectedServers;
    private final AtomicLong requestId;
    private final int connectTimeout;
    private final int readTimeout;

    public HttpMcpServerClient()
    {
        this(5000, 30000);
    }

    public HttpMcpServerClient(int connectTimeout, int readTimeout)
    {
        this.objectMapper = new ObjectMapper();
        this.sessionIds = new ConcurrentHashMap<>();
        this.connectedServers = new ConcurrentHashMap<>();
        this.requestId = new AtomicLong(0);
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public void connect(McpServerConfiguration config)
    {
        if (connectedServers.containsKey(config.getServerId()))
        {
            return;
        }

        try
        {
            System.out.println("[MCP-HTTP] Connecting to: " + config.getEndpoint());

            // Generate a session ID for this connection
            String sessionId = UUID.randomUUID().toString();
            sessionIds.put(config.getServerId(), sessionId);

            // Initialize the connection
            initialize(config);
            connectedServers.put(config.getServerId(), true);
            System.out.println("[MCP-HTTP] Connected successfully");
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Failed to connect to MCP server: " + config.getServerId(), ex);
        }
    }

    private void initialize(McpServerConfiguration config) throws Exception
    {
        ObjectNode initRequest = createRequest("initialize");
        ObjectNode params = initRequest.putObject("params");
        ObjectNode clientInfo = params.putObject("clientInfo");
        clientInfo.put("name", "generic-agent-core");
        clientInfo.put("version", "1.0.0");
        params.put("protocolVersion", "2024-11-05");
        ObjectNode capabilities = params.putObject("capabilities");
        capabilities.putObject("tools");

        System.out.println("[MCP-HTTP] Sending initialize request");
        JsonNode response = sendRequest(config, initRequest);
        System.out.println("[MCP-HTTP] Initialized successfully");

        // Send initialized notification
        ObjectNode initializedRequest = objectMapper.createObjectNode();
        initializedRequest.put("jsonrpc", "2.0");
        initializedRequest.put("method", "notifications/initialized");
        sendNotification(config, initializedRequest);
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
                args.forEach((key, value) ->
                {
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
        if (!connectedServers.containsKey(config.getServerId()))
        {
            connect(config);
        }
    }

    private ObjectNode createRequest(String method)
    {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", String.valueOf(requestId.incrementAndGet()));
        request.put("method", method);
        return request;
    }

    private JsonNode sendRequest(McpServerConfiguration config, ObjectNode request) throws Exception
    {
        String endpoint = config.getEndpoint().replaceAll("/$", "");
        String messageEndpoint = endpoint + "/messages/";

        String body = objectMapper.writeValueAsString(request);
        System.out.println("[MCP-HTTP] Sending: " + body);

        URL url = new URL(messageEndpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);

        try (OutputStream os = connection.getOutputStream())
        {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_ACCEPTED)
        {
            try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8)))
            {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null)
                {
                    errorResponse.append(line);
                }
                throw new RuntimeException("HTTP request failed with code: " + responseCode + ", error: " + errorResponse);
            }
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)))
        {
            String responseLine;
            while ((responseLine = br.readLine()) != null)
            {
                // Handle SSE format
                if (responseLine.startsWith("data:"))
                {
                    response.append(responseLine.substring(5).trim());
                }
                else if (!responseLine.isEmpty() && !responseLine.startsWith(":"))
                {
                    response.append(responseLine.trim());
                }
            }
        }

        String responseBody = response.toString();
        System.out.println("[MCP-HTTP] Received: " + responseBody);
        return objectMapper.readTree(responseBody);
    }

    private void sendNotification(McpServerConfiguration config, ObjectNode notification) throws Exception
    {
        String endpoint = config.getEndpoint().replaceAll("/$", "");
        String messageEndpoint = endpoint + "/messages/";

        String body = objectMapper.writeValueAsString(notification);
        System.out.println("[MCP-HTTP] Sending notification: " + body);

        URL url = new URL(messageEndpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);

        try (OutputStream os = connection.getOutputStream())
        {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        connection.getResponseCode();
    }

    public void disconnect(McpServerConfiguration config)
    {
        connectedServers.remove(config.getServerId());
        sessionIds.remove(config.getServerId());
        System.out.println("[MCP-HTTP] Disconnected from: " + config.getServerId());
    }
}
