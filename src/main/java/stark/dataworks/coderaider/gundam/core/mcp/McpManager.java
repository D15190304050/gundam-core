package stark.dataworks.coderaider.gundam.core.mcp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import stark.dataworks.coderaider.gundam.core.mcp.approval.AllowAllMcpToolApprovalPolicy;
import stark.dataworks.coderaider.gundam.core.mcp.approval.IMcpToolApprovalPolicy;
import stark.dataworks.coderaider.gundam.core.mcp.approval.McpToolApprovalRequest;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolParameterSchema;

/**
 * McpManager implements MCP server integration and tool bridging.
 */
@AllArgsConstructor
public class McpManager
{

    /**
 * Registered MCP server configurations keyed by server id.
     */
    private final Map<String, McpServerConfiguration> servers = new ConcurrentHashMap<>();

    /**
     * MCP/LLM client used for remote calls.
     */
    private final IMcpServerClient client;

    /**
     * Policy used to approve or block tool execution.
     */
    private final IMcpToolApprovalPolicy approvalPolicy;

    /**
     * Performs mcp manager as part of McpManager runtime responsibilities.
     * @param client The client used by this operation.
     */
    public McpManager(IMcpServerClient client)
    {
        this(client, new AllowAllMcpToolApprovalPolicy());
    }

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param serverConfig The server config used by this operation.
     */
    public void registerServer(McpServerConfiguration serverConfig)
    {
        servers.put(serverConfig.getServerId(), serverConfig);
    }

    /**
     * Performs list tools as part of McpManager runtime responsibilities.
     * @param serverId The server id used by this operation.
     * @return The value produced by this operation.
     */
    public List<McpToolDescriptor> listTools(String serverId)
    {
        McpServerConfiguration config = requireServer(serverId);
        return client.listTools(config);
    }

    /**
     * Performs list resources as part of McpManager runtime responsibilities.
     * @param serverId The server id used by this operation.
     * @return The value produced by this operation.
     */
    public List<McpResource> listResources(String serverId)
    {
        return client.listResources(requireServer(serverId));
    }

    /**
     * Performs list resource templates as part of McpManager runtime responsibilities.
     * @param serverId The server id used by this operation.
     * @return The value produced by this operation.
     */
    public List<McpResourceTemplate> listResourceTemplates(String serverId)
    {
        return client.listResourceTemplates(requireServer(serverId));
    }

    /**
     * Performs read resource as part of McpManager runtime responsibilities.
     * @param serverId The server id used by this operation.
     * @param uri The uri used by this operation.
     * @return The value produced by this operation.
     */
    public McpResource readResource(String serverId, String uri)
    {
        return client.readResource(requireServer(serverId), uri);
    }

    /**
     * Resolves tools as local tools from configured registries before execution continues.
     * @param serverId The server id used by this operation.
     * @return The value produced by this operation.
     */
    public List<ITool> resolveToolsAsLocalTools(String serverId)
    {
        McpServerConfiguration config = requireServer(serverId);
        List<ITool> tools = new ArrayList<>();
        for (McpToolDescriptor descriptor : client.listTools(config))
        {
            tools.add(new McpProxyTool(config, descriptor, client, approvalPolicy));
        }
        return tools;
    }

    /**
     * Performs require server as part of McpManager runtime responsibilities.
     * @param serverId The server id used by this operation.
     * @return The value produced by this operation.
     */
    private McpServerConfiguration requireServer(String serverId)
    {
        McpServerConfiguration config = servers.get(serverId);
        if (config == null)
        {
            throw new IllegalArgumentException("MCP server not registered: " + serverId);
        }
        return config;
    }

    /**
     * McpProxyTool implements MCP server integration and tool bridging.
     *     */
    private static class McpProxyTool implements ITool
    {

        /**
         * Processor configuration map for this workflow vertex.
         */
        private final McpServerConfiguration config;

        /**
         * MCP tool descriptor discovered from the remote server.
         */
        private final McpToolDescriptor descriptor;

        /**
         * MCP/LLM client used for remote calls.
         */
        private final IMcpServerClient client;

        /**
         * Policy used to approve or block tool execution.
         */
        private final IMcpToolApprovalPolicy approvalPolicy;

        /**
         * Performs mcp proxy tool as part of McpManager runtime responsibilities.
         * @param config The config used by this operation.
         * @param descriptor The descriptor used by this operation.
         * @param client The client used by this operation.
         * @param approvalPolicy The approval policy used by this operation.
         */
        private McpProxyTool(McpServerConfiguration config, McpToolDescriptor descriptor, IMcpServerClient client, IMcpToolApprovalPolicy approvalPolicy)
        {
            this.config = config;
            this.descriptor = descriptor;
            this.client = client;
            this.approvalPolicy = approvalPolicy;
        }

        /**
         * Performs definition as part of McpManager runtime responsibilities.
         * @return The value produced by this operation.
         */
        @Override
        public ToolDefinition definition()
        {
            return new ToolDefinition(descriptor.getName(), descriptor.getDescription(), convertInputSchema(descriptor.getInputSchema()));
        }

        /**
         * Converts MCP input schema to tool parameter schemas.
         * @param inputSchema The input schema from MCP tool descriptor.
         * @return List of tool parameter schemas.
         */
        @SuppressWarnings("unchecked")
        private List<ToolParameterSchema> convertInputSchema(Map<String, Object> inputSchema)
        {
            if (inputSchema == null || !inputSchema.containsKey("properties"))
            {
                return List.of();
            }

            List<ToolParameterSchema> params = new ArrayList<>();
            Object propsObj = inputSchema.get("properties");
            if (!(propsObj instanceof Map))
            {
                return List.of();
            }

            Map<String, Object> properties = (Map<String, Object>) propsObj;
            Set<String> requiredFields = new HashSet<>();
            Object requiredObj = inputSchema.get("required");
            if (requiredObj instanceof List)
            {
                requiredFields.addAll((List<String>) requiredObj);
            }

            for (Map.Entry<String, Object> entry : properties.entrySet())
            {
                String paramName = entry.getKey();
                Object paramDef = entry.getValue();
                if (!(paramDef instanceof Map))
                {
                    continue;
                }

                Map<String, Object> paramInfo = (Map<String, Object>) paramDef;
                String type = (String) paramInfo.getOrDefault("type", "string");
                String description = (String) paramInfo.getOrDefault("description", "");
                boolean required = requiredFields.contains(paramName);
                params.add(new ToolParameterSchema(paramName, type, required, description));
            }

            return params;
        }

        /**
         * Runs the primary execution flow, coordinating model/tool work and runtime policies.
         * @param input The input used by this operation.
         * @return The value produced by this operation.
         */
        @Override
        public String execute(Map<String, Object> input)
        {
            var decision = approvalPolicy.decide(new McpToolApprovalRequest(config.getServerId(), descriptor.getName(), input));
            if (!decision.approved())
            {
                return "MCP tool denied: " + decision.reason();
            }
            return client.callTool(config, descriptor.getName(), input);
        }
    }
}
