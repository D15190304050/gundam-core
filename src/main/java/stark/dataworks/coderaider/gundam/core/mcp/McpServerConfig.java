package stark.dataworks.coderaider.gundam.core.mcp;

import java.util.Map;
import java.util.Objects;
/**
 * Class McpServerConfig.
 */

public class McpServerConfig
{
    /**
     * Field serverId.
     */
    private final String serverId;
    /**
     * Field endpoint.
     */
    private final String endpoint;
    /**
     * Field options.
     */
    private final Map<String, Object> options;
    /**
     * Creates a new McpServerConfig instance.
     */

    public McpServerConfig(String serverId, String endpoint, Map<String, Object> options)
    {
        this.serverId = Objects.requireNonNull(serverId, "serverId");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.options = options == null ? Map.of() : Map.copyOf(options);
    }
    /**
     * Executes getServerId.
     */

    public String getServerId()
    {
        return serverId;
    }
    /**
     * Executes getEndpoint.
     */

    public String getEndpoint()
    {
        return endpoint;
    }
    /**
     * Executes getOptions.
     */

    public Map<String, Object> getOptions()
    {
        return options;
    }
}
