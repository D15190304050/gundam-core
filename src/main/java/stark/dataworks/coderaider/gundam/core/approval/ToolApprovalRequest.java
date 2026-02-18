package stark.dataworks.coderaider.gundam.core.approval;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
/**
 * Class ToolApprovalRequest.
 */

public class ToolApprovalRequest
{
    /**
     * Field agentId.
     */
    private final String agentId;
    /**
     * Field toolName.
     */
    private final String toolName;
    /**
     * Field arguments.
     */
    private final Map<String, Object> arguments;
    /**
     * Creates a new ToolApprovalRequest instance.
     */

    public ToolApprovalRequest(String agentId, String toolName, Map<String, Object> arguments)
    {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.toolName = Objects.requireNonNull(toolName, "toolName");
        this.arguments = Collections.unmodifiableMap(arguments == null ? Map.of() : arguments);
    }
    /**
     * Executes getAgentId.
     */

    public String getAgentId()
    {
        return agentId;
    }
    /**
     * Executes getToolName.
     */

    public String getToolName()
    {
        return toolName;
    }
    /**
     * Executes getArguments.
     */

    public Map<String, Object> getArguments()
    {
        return arguments;
    }
}
