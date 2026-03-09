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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SSE-based MCP server client that connects to real MCP servers using Server-Sent Events transport.
 */
public class SseMcpServerClient implements IMcpServerClient
{
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration timeout;
    private final Map<String, String> messageEndpoints;
    private final Map<String, BlockingQueue<JsonNode>> pendingResponses;
    private final Map<String, Thread> streamReaders;
    private final AtomicLong requestIdCounter;

    public SseMcpServerClient()
    {
        this(Duration.ofSeconds(30));
    }

    public SseMcpServerClient(Duration timeout)
    {
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
        this.objectMapper = new ObjectMapper();
        this.messageEndpoints = new ConcurrentHashMap<>();
        this.pendingResponses = new ConcurrentHashMap<>();
        this.streamReaders = new ConcurrentHashMap<>();
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

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8));
            String messageEndpoint = waitForEndpoint(reader, baseUrl);
            if (messageEndpoint == null)
            {
                throw new RuntimeException("Failed to get message endpoint from MCP server");
            }

            messageEndpoints.put(config.getServerId(), messageEndpoint);
            Thread readerThread = new Thread(() -> readSseMessages(reader), "mcp-sse-" + config.getServerId());
            readerThread.setDaemon(true);
            readerThread.start();
            streamReaders.put(config.getServerId(), readerThread);

            initialize(config);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Failed to connect to MCP server: " + config.getServerId(), ex);
        }
    }

    private String waitForEndpoint(BufferedReader reader, String baseUrl) throws Exception
    {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        String currentEvent = "";
        String line;
        while (System.currentTimeMillis() < deadline && (line = reader.readLine()) != null)
        {
            if (line.startsWith("event:"))
            {
                currentEvent = line.substring("event:".length()).trim();
            }
            else if (line.startsWith("data:"))
            {
                String data = line.substring("data:".length()).trim();
                if ("endpoint".equals(currentEvent))
                {
                    if (!data.startsWith("http"))
                    {
                        return baseUrl + data;
                    }
                    return data;
                }
            }
        }
        return null;
    }

    private void readSseMessages(BufferedReader reader)
    {
        try
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (!line.startsWith("data:"))
                {
                    continue;
                }
                String data = line.substring("data:".length()).trim();
                if (data.isBlank())
                {
                    continue;
                }
                JsonNode payload = objectMapper.readTree(data);
                JsonNode idNode = payload.get("id");
                if (idNode != null)
                {
                    String id = idNode.asText();
                    BlockingQueue<JsonNode> queue = pendingResponses.get(id);
                    if (queue != null)
                    {
                        queue.offer(payload);
                    }
                }
            }
        }
        catch (Exception ignored)
        {
            // stream ended/disconnected
        }
    }

    private void initialize(McpServerConfiguration config)
    {
        try
        {
            ObjectNode initRequest = createRequest("initialize");
            ObjectNode params = initRequest.putObject("params");
            ObjectNode clientInfo = params.putObject("clientInfo");
            clientInfo.put("name", "generic-agent-core");
            clientInfo.put("version", "1.0.0");
            params.put("protocolVersion", "2024-11-05");
            params.putObject("capabilities").putObject("tools");

            JsonNode response = sendRequest(config, initRequest);
            if (response.has("error"))
            {
                throw new RuntimeException("Initialize failed: " + response.path("error"));
            }

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
                args.forEach((key, value) -> argsNode.set(key, objectMapper.valueToTree(value)));
            }

            JsonNode response = sendRequest(config, request);
            JsonNode content = response.path("result").path("content");
            if (content.isArray() && content.size() > 0)
            {
                StringBuilder result = new StringBuilder();
                for (JsonNode item : content)
                {
                    if ("text".equals(item.path("type").asText()))
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
                return new McpResource(uri, first.path("mimeType").asText("text/plain"), first.path("text").asText(""));
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

        String requestId = request.path("id").asText();
        BlockingQueue<JsonNode> queue = new LinkedBlockingQueue<>(1);
        pendingResponses.put(requestId, queue);
        try
        {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(messageEndpoint))
                .header("Content-Type", "application/json")
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400)
            {
                throw new RuntimeException("MCP server returned status " + response.statusCode() + ": " + response.body());
            }

            JsonNode payload = queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (payload == null)
            {
                throw new RuntimeException("Timed out waiting for SSE response for request id " + requestId);
            }
            return payload;
        }
        finally
        {
            pendingResponses.remove(requestId);
        }
    }

    private void sendNotification(McpServerConfiguration config, ObjectNode notification) throws Exception
    {
        String messageEndpoint = messageEndpoints.get(config.getServerId());
        if (messageEndpoint == null)
        {
            throw new RuntimeException("Not connected to MCP server: " + config.getServerId());
        }

        notification.remove("id");
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(messageEndpoint))
            .header("Content-Type", "application/json")
            .timeout(timeout)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(notification)))
            .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400)
        {
            throw new RuntimeException("MCP server returned status " + response.statusCode() + ": " + response.body());
        }
    }
}
