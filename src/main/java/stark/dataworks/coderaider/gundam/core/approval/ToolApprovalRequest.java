package stark.dataworks.coderaider.gundam.core.approval;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * ToolApprovalRequest implements tool approval workflow.
 */
@Getter
public class ToolApprovalRequest
{

    /**
     * Identifier of the agent associated with this operation.
     */
    private final String agentId;

    /**
     * Name of the tool being requested or executed.
     */
    private final String toolName;

    /**
     * Tool-call arguments provided by the model/caller.
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
}
