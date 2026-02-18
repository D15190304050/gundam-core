package stark.dataworks.coderaider.gundam.core.tool;

import java.util.Map;

/**
 * ToolExecutionContext implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class ToolExecutionContext
{

    /**
     * Internal state for agent id; used while coordinating runtime behavior.
     */
    private final String agentId;

    /**
     * Internal state for runtime metadata; used while coordinating runtime behavior.
     */
    private final Map<String, Object> runtimeMetadata;

    /**
     * Performs tool execution context as part of ToolExecutionContext runtime responsibilities.
     * @param agentId The agent id used by this operation.
     * @param runtimeMetadata The runtime metadata used by this operation.
     */
    public ToolExecutionContext(String agentId, Map<String, Object> runtimeMetadata)
    {
        this.agentId = agentId;
        this.runtimeMetadata = runtimeMetadata == null ? Map.of() : Map.copyOf(runtimeMetadata);
    }

    /**
     * Returns the current agent id value maintained by this ToolExecutionContext.
     * @return The value produced by this operation.
     */
    public String getAgentId()
    {
        return agentId;
    }

    /**
     * Returns the current runtime metadata value maintained by this ToolExecutionContext.
     * @return The value produced by this operation.
     */
    public Map<String, Object> getRuntimeMetadata()
    {
        return runtimeMetadata;
    }
}
