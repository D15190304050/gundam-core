package stark.dataworks.coderaider.gundam.core.mcp;

import lombok.Getter;

import java.util.Map;
import java.util.Objects;

/**
 * McpServerConfig implements MCP server integration and tool bridging.
 */
@Getter
public class McpServerConfiguration
{

    /**
     * Unique MCP server identifier used for lookup/routing.
     */
    private final String serverId;

    /**
     * Connection endpoint for the MCP server.
     */
    private final String endpoint;

    /**
     * Provider/server options forwarded without transformation.
     */
    private final Map<String, Object> options;

    /**
     * Performs mcp server config as part of McpServerConfig runtime responsibilities.
     * @param serverId The server id used by this operation.
     * @param endpoint The endpoint used by this operation.
     * @param options The options used by this operation.
     */
    public McpServerConfiguration(String serverId, String endpoint, Map<String, Object> options)
    {
        this.serverId = Objects.requireNonNull(serverId, "serverId");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.options = options == null ? Map.of() : Map.copyOf(options);
    }
}
