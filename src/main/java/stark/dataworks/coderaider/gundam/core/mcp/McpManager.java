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
 * Class McpManager.
 */

public class McpManager
{
    /**
     * Field servers.
     */
    private final Map<String, McpServerConfig> servers = new ConcurrentHashMap<>();
    /**
     * Field client.
     */
    private final McpServerClient client;
    /**
     * Field approvalPolicy.
     */
    private final McpToolApprovalPolicy approvalPolicy;
    /**
     * Creates a new McpManager instance.
     */

    public McpManager(McpServerClient client)
    {
        this(client, new AllowAllMcpToolApprovalPolicy());
    }
    /**
     * Creates a new McpManager instance.
     */

    public McpManager(McpServerClient client, McpToolApprovalPolicy approvalPolicy)
    {
        this.client = client;
        this.approvalPolicy = approvalPolicy;
    }
    /**
     * Executes registerServer.
     */

    public void registerServer(McpServerConfig serverConfig)
    {
        servers.put(serverConfig.getServerId(), serverConfig);
    }
    /**
     * Executes listTools.
     */

    public List<McpToolDescriptor> listTools(String serverId)
    {
        McpServerConfig config = requireServer(serverId);
        return client.listTools(config);
    }
    /**
     * Executes listResources.
     */

    public List<McpResource> listResources(String serverId)
    {
        return client.listResources(requireServer(serverId));
    }
    /**
     * Executes listResourceTemplates.
     */

    public List<McpResourceTemplate> listResourceTemplates(String serverId)
    {
        return client.listResourceTemplates(requireServer(serverId));
    }
    /**
     * Executes readResource.
     */

    public McpResource readResource(String serverId, String uri)
    {
        return client.readResource(requireServer(serverId), uri);
    }
    /**
     * Executes resolveToolsAsLocalTools.
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
     * Executes requireServer.
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
     * Class McpProxyTool.
     */

    private static class McpProxyTool implements ITool
    {
        /**
         * Field config.
         */
        private final McpServerConfig config;
        /**
         * Field descriptor.
         */
        private final McpToolDescriptor descriptor;
        /**
         * Field client.
         */
        private final McpServerClient client;
        /**
         * Field approvalPolicy.
         */
        private final McpToolApprovalPolicy approvalPolicy;
        /**
         * Creates a new McpProxyTool instance.
         */

        private McpProxyTool(McpServerConfig config, McpToolDescriptor descriptor, McpServerClient client, McpToolApprovalPolicy approvalPolicy)
        {
            this.config = config;
            this.descriptor = descriptor;
            this.client = client;
            this.approvalPolicy = approvalPolicy;
        }

        /**
         * Executes definition.
         */
        @Override
        public ToolDefinition definition()
        {
            return new ToolDefinition(descriptor.getName(), descriptor.getDescription(), List.of());
        }

        /**
         * Executes execute.
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
