package stark.dataworks.coderaider.gundam.core.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Stdio-based MCP server client that connects to MCP servers using standard input/output.
 * <p>
 * This client implements the MCP (Model Context Protocol) JSON-RPC 2.0 protocol
 * over stdio, supporting tool listing and invocation.
 * <p>
 * Usage:
 * <pre>
 * StdioMcpServerClient client = new StdioMcpServerClient();
 * McpServerConfiguration config = new McpServerConfiguration("my-server", "python path/to/server.py", Map.of());
 * client.connect(config);
 * List<McpToolDescriptor> tools = client.listTools(config);
 * String result = client.callTool(config, "add", Map.of("a", 1, "b", 2));
 * </pre>
 */
public class StdioMcpServerClient implements IMcpServerClient
{
    private final ObjectMapper objectMapper;
    private final Map<String, Process> processes;
    private final Map<String, BufferedWriter> writers;
    private final Map<String, BufferedReader> readers;
    private final ExecutorService executor;
    private final long timeoutSeconds;

    public StdioMcpServerClient()
    {
        this(30);
    }

    public StdioMcpServerClient(long timeoutSeconds)
    {
        this.objectMapper = new ObjectMapper();
        this.processes = new HashMap<>();
        this.writers = new HashMap<>();
        this.readers = new HashMap<>();
        this.executor = Executors.newCachedThreadPool();
        this.timeoutSeconds = timeoutSeconds;
    }

    public void connect(McpServerConfiguration config)
    {
        if (processes.containsKey(config.getServerId()))
        {
            return;
        }
        
        try
        {
            String command = config.getEndpoint();
            System.out.println("[MCP] Starting process: " + command);
            
            ProcessBuilder pb = new ProcessBuilder(command.split("\\s+"));
            pb.redirectErrorStream(false);
            Process process = pb.start();
            
            processes.put(config.getServerId(), process);
            writers.put(config.getServerId(), new BufferedWriter(new OutputStreamWriter(process.getOutputStream())));
            readers.put(config.getServerId(), new BufferedReader(new InputStreamReader(process.getInputStream())));
            
            System.out.println("[MCP] Process started, initializing...");
            initialize(config);
            System.out.println("[MCP] Initialized successfully");
        }
        catch (Exception ex)
        {
            cleanup(config);
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
            ObjectNode capabilities = params.putObject("capabilities");
            capabilities.putObject("tools");
            
            System.out.println("[MCP] Sending initialize request: " + objectMapper.writeValueAsString(initRequest));
            JsonNode response = sendRequest(config, initRequest);
            System.out.println("[MCP] Initialize response: " + objectMapper.writeValueAsString(response));
            
            ObjectNode initializedRequest = createRequest("notifications/initialized");
            initializedRequest.remove("id");
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
        if (!processes.containsKey(config.getServerId()))
        {
            connect(config);
        }
    }

    private ObjectNode createRequest(String method)
    {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", String.valueOf(System.currentTimeMillis()));
        request.put("method", method);
        return request;
    }

    private JsonNode sendRequest(McpServerConfiguration config, ObjectNode request) throws Exception
    {
        BufferedWriter writer = writers.get(config.getServerId());
        BufferedReader reader = readers.get(config.getServerId());
        
        if (writer == null || reader == null)
        {
            throw new RuntimeException("Not connected to MCP server: " + config.getServerId());
        }
        
        String body = objectMapper.writeValueAsString(request);
        System.out.println("[MCP] Sending: " + body);
        
        writer.write(body);
        writer.newLine();
        writer.flush();
        
        Future<String> future = executor.submit(() -> reader.readLine());
        
        try
        {
            String responseLine = future.get(timeoutSeconds, TimeUnit.SECONDS);
            if (responseLine == null)
            {
                throw new RuntimeException("No response from MCP server: " + config.getServerId());
            }
            System.out.println("[MCP] Received: " + responseLine);
            return objectMapper.readTree(responseLine);
        }
        catch (TimeoutException e)
        {
            future.cancel(true);
            throw new RuntimeException("Timeout waiting for response from MCP server: " + config.getServerId());
        }
    }

    private void sendNotification(McpServerConfiguration config, ObjectNode notification) throws Exception
    {
        BufferedWriter writer = writers.get(config.getServerId());
        
        if (writer == null)
        {
            throw new RuntimeException("Not connected to MCP server: " + config.getServerId());
        }
        
        String body = objectMapper.writeValueAsString(notification);
        System.out.println("[MCP] Sending notification: " + body);
        
        writer.write(body);
        writer.newLine();
        writer.flush();
    }

    private void cleanup(McpServerConfiguration config)
    {
        Process process = processes.remove(config.getServerId());
        writers.remove(config.getServerId());
        readers.remove(config.getServerId());
        
        if (process != null)
        {
            process.destroyForcibly();
        }
    }

    public void disconnect(McpServerConfiguration config)
    {
        cleanup(config);
    }
}
