package stark.dataworks.coderaider.gundam.core.tool;

import java.util.Map;
/**
 * Class ToolExecutionContext.
 */

public class ToolExecutionContext
{
    /**
     * Field agentId.
     */
    private final String agentId;
    /**
     * Field runtimeMetadata.
     */
    private final Map<String, Object> runtimeMetadata;
    /**
     * Creates a new ToolExecutionContext instance.
     */

    public ToolExecutionContext(String agentId, Map<String, Object> runtimeMetadata)
    {
        this.agentId = agentId;
        this.runtimeMetadata = runtimeMetadata == null ? Map.of() : Map.copyOf(runtimeMetadata);
    }
    /**
     * Executes getAgentId.
     */

    public String getAgentId()
    {
        return agentId;
    }
    /**
     * Executes getRuntimeMetadata.
     */

    public Map<String, Object> getRuntimeMetadata()
    {
        return runtimeMetadata;
    }
}
