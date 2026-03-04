package stark.dataworks.coderaider.gundam.core.tool;

import lombok.Getter;

import java.util.Map;

/**
 * ToolExecutionContext implements tool contracts, schema metadata, and executable tool registration.
 */
@Getter
public class ToolExecutionContext
{

    /**
     * Identifier of the agent associated with this operation.
     */
    private final String agentId;

    /**
     * Arbitrary runtime metadata passed to tool execution.
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
}
