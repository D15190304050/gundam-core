package stark.dataworks.coderaider.gundam.core.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import stark.dataworks.coderaider.gundam.core.mcp.approval.AllowAllMcpToolApprovalPolicy;
import stark.dataworks.coderaider.gundam.core.mcp.approval.McpToolApprovalPolicy;
import stark.dataworks.coderaider.gundam.core.mcp.approval.McpToolApprovalRequest;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * McpManager implements MCP server integration and tool bridging.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class McpManager
{

    /**
     * Internal state for servers used while coordinating runtime behavior.
     */
    private final Map<String, McpServerConfig> servers = new ConcurrentHashMap<>();

    /**
     * Internal state for client; used while coordinating runtime behavior.
     */
    private final McpServerClient client;

    /**
     * Internal state for approval policy; used while coordinating runtime behavior.
     */
    private final McpToolApprovalPolicy approvalPolicy;

    /**
     * Performs mcp manager as part of McpManager runtime responsibilities.
     * @param client The client used by this operation.
     */
    public McpManager(McpServerClient client)
    {
        this(client, new AllowAllMcpToolApprovalPolicy());
    }

    /**
     * Performs mcp manager as part of McpManager runtime responsibilities.
     * @param client The client used by this operation.
     * @param approvalPolicy The approval policy used by this operation.
     */
    public McpManager(McpServerClient client, McpToolApprovalPolicy approvalPolicy)
    {
        this.client = client;
        this.approvalPolicy = approvalPolicy;
    }

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param serverConfig The server config used by this operation.
     */
    public void registerServer(McpServerConfig serverConfig)
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
        McpServerConfig config = requireServer(serverId);
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
        McpServerConfig config = requireServer(serverId);
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
    private McpServerConfig requireServer(String serverId)
    {
        McpServerConfig config = servers.get(serverId);
        if (config == null)
        {
            throw new IllegalArgumentException("MCP server not registered: " + serverId);
        }
        return config;
    }

    /**
     * McpProxyTool implements MCP server integration and tool bridging.
     * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
     */
    private static class McpProxyTool implements ITool
    {

        /**
         * Internal state for config; used while coordinating runtime behavior.
         */
        private final McpServerConfig config;

        /**
         * Internal state for descriptor; used while coordinating runtime behavior.
         */
        private final McpToolDescriptor descriptor;

        /**
         * Internal state for client; used while coordinating runtime behavior.
         */
        private final McpServerClient client;

        /**
         * Internal state for approval policy; used while coordinating runtime behavior.
         */
        private final McpToolApprovalPolicy approvalPolicy;

        /**
         * Performs mcp proxy tool as part of McpManager runtime responsibilities.
         * @param config The config used by this operation.
         * @param descriptor The descriptor used by this operation.
         * @param client The client used by this operation.
         * @param approvalPolicy The approval policy used by this operation.
         */
        private McpProxyTool(McpServerConfig config, McpToolDescriptor descriptor, McpServerClient client, McpToolApprovalPolicy approvalPolicy)
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
            return new ToolDefinition(descriptor.getName(), descriptor.getDescription(), List.of());
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
