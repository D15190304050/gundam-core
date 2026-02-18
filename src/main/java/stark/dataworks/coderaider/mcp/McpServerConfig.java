package stark.dataworks.coderaider.mcp;

import java.util.Map;
import java.util.Objects;

public class McpServerConfig {
    private final String serverId;
    private final String endpoint;
    private final Map<String, Object> options;

    public McpServerConfig(String serverId, String endpoint, Map<String, Object> options) {
        this.serverId = Objects.requireNonNull(serverId, "serverId");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.options = options == null ? Map.of() : Map.copyOf(options);
    }

    public String getServerId() {
        return serverId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Map<String, Object> getOptions() {
        return options;
    }
}
