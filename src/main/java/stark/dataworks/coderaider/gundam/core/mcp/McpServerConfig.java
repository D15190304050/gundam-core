package stark.dataworks.coderaider.gundam.core.mcp;

import java.util.Map;
import java.util.Objects;

/**
 * McpServerConfig implements MCP server integration and tool bridging.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class McpServerConfig
{

    /**
     * Internal state for server id; used while coordinating runtime behavior.
     */
    private final String serverId;

    /**
     * Internal state for endpoint; used while coordinating runtime behavior.
     */
    private final String endpoint;

    /**
     * Internal state for options; used while coordinating runtime behavior.
     */
    private final Map<String, Object> options;

    /**
     * Performs mcp server config as part of McpServerConfig runtime responsibilities.
     * @param serverId The server id used by this operation.
     * @param endpoint The endpoint used by this operation.
     * @param options The options used by this operation.
     */
    public McpServerConfig(String serverId, String endpoint, Map<String, Object> options)
    {
        this.serverId = Objects.requireNonNull(serverId, "serverId");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.options = options == null ? Map.of() : Map.copyOf(options);
    }

    /**
     * Returns the current server id value maintained by this McpServerConfig.
     * @return The value produced by this operation.
     */
    public String getServerId()
    {
        return serverId;
    }

    /**
     * Returns the current endpoint value maintained by this McpServerConfig.
     * @return The value produced by this operation.
     */
    public String getEndpoint()
    {
        return endpoint;
    }

    /**
     * Returns the current options value maintained by this McpServerConfig.
     * @return The value produced by this operation.
     */
    public Map<String, Object> getOptions()
    {
        return options;
    }
}
