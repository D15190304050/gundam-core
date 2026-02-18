package stark.dataworks.coderaider.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import stark.dataworks.coderaider.mcp.approval.AllowAllMcpToolApprovalPolicy;
import stark.dataworks.coderaider.mcp.approval.McpToolApprovalPolicy;
import stark.dataworks.coderaider.mcp.approval.McpToolApprovalRequest;
import stark.dataworks.coderaider.tool.ITool;
import stark.dataworks.coderaider.tool.ToolDefinition;

public class McpManager {
    private final Map<String, McpServerConfig> servers = new ConcurrentHashMap<>();
    private final McpServerClient client;
    private final McpToolApprovalPolicy approvalPolicy;

    public McpManager(McpServerClient client) {
        this(client, new AllowAllMcpToolApprovalPolicy());
    }

    public McpManager(McpServerClient client, McpToolApprovalPolicy approvalPolicy) {
        this.client = client;
        this.approvalPolicy = approvalPolicy;
    }

    public void registerServer(McpServerConfig serverConfig) {
        servers.put(serverConfig.getServerId(), serverConfig);
    }

    public List<McpToolDescriptor> listTools(String serverId) {
        McpServerConfig config = requireServer(serverId);
        return client.listTools(config);
    }

    public List<McpResource> listResources(String serverId) {
        return client.listResources(requireServer(serverId));
    }

    public List<McpResourceTemplate> listResourceTemplates(String serverId) {
        return client.listResourceTemplates(requireServer(serverId));
    }

    public McpResource readResource(String serverId, String uri) {
        return client.readResource(requireServer(serverId), uri);
    }

    public List<ITool> resolveToolsAsLocalTools(String serverId) {
        McpServerConfig config = requireServer(serverId);
        List<ITool> tools = new ArrayList<>();
        for (McpToolDescriptor descriptor : client.listTools(config)) {
            tools.add(new McpProxyTool(config, descriptor, client, approvalPolicy));
        }
        return tools;
    }

    private McpServerConfig requireServer(String serverId) {
        McpServerConfig config = servers.get(serverId);
        if (config == null) {
            throw new IllegalArgumentException("MCP server not registered: " + serverId);
        }
        return config;
    }

    private static class McpProxyTool implements ITool {
        private final McpServerConfig config;
        private final McpToolDescriptor descriptor;
        private final McpServerClient client;
        private final McpToolApprovalPolicy approvalPolicy;

        private McpProxyTool(McpServerConfig config, McpToolDescriptor descriptor, McpServerClient client, McpToolApprovalPolicy approvalPolicy) {
            this.config = config;
            this.descriptor = descriptor;
            this.client = client;
            this.approvalPolicy = approvalPolicy;
        }

        @Override
        public ToolDefinition definition() {
            return new ToolDefinition(descriptor.getName(), descriptor.getDescription(), List.of());
        }

        @Override
        public String execute(Map<String, Object> input) {
            var decision = approvalPolicy.decide(new McpToolApprovalRequest(config.getServerId(), descriptor.getName(), input));
            if (!decision.approved()) {
                return "MCP tool denied: " + decision.reason();
            }
            return client.callTool(config, descriptor.getName(), input);
        }
    }
}
