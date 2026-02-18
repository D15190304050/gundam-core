package stark.dataworks.coderaider.gundam.core.approval;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * ToolApprovalRequest implements tool approval workflow.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class ToolApprovalRequest
{

    /**
     * Internal state for agent id; used while coordinating runtime behavior.
     */
    private final String agentId;

    /**
     * Internal state for tool name; used while coordinating runtime behavior.
     */
    private final String toolName;

    /**
     * Internal state for arguments; used while coordinating runtime behavior.
     */
    private final Map<String, Object> arguments;

    /**
     * Performs tool approval request as part of ToolApprovalRequest runtime responsibilities.
     * @param agentId The agent id used by this operation.
     * @param toolName The tool name used by this operation.
     * @param arguments The arguments used by this operation.
     */
    public ToolApprovalRequest(String agentId, String toolName, Map<String, Object> arguments)
    {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.toolName = Objects.requireNonNull(toolName, "toolName");
        this.arguments = Collections.unmodifiableMap(arguments == null ? Map.of() : arguments);
    }

    /**
     * Returns the current agent id value maintained by this ToolApprovalRequest.
     * @return The value produced by this operation.
     */
    public String getAgentId()
    {
        return agentId;
    }

    /**
     * Returns the current tool name value maintained by this ToolApprovalRequest.
     * @return The value produced by this operation.
     */
    public String getToolName()
    {
        return toolName;
    }

    /**
     * Returns the current arguments value maintained by this ToolApprovalRequest.
     * @return The value produced by this operation.
     */
    public Map<String, Object> getArguments()
    {
        return arguments;
    }
}
