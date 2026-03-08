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
     * Initializes ToolExecutionContext with required runtime dependencies and options.
     *
     * @param agentId         agent identifier.
     * @param runtimeMetadata runtime metadata.
     */
    public ToolExecutionContext(String agentId, Map<String, Object> runtimeMetadata)
    {
        this.agentId = agentId;
        this.runtimeMetadata = runtimeMetadata == null ? Map.of() : Map.copyOf(runtimeMetadata);
    }
}
