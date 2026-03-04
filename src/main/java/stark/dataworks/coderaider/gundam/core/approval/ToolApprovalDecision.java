package stark.dataworks.coderaider.gundam.core.approval;

import lombok.Getter;

import java.util.Objects;

/**
 * ToolApprovalDecision implements tool approval workflow.
 */
@Getter
public class ToolApprovalDecision
{

    /**
     * Whether the tool can be executed.
     */
    private final boolean approved;

    /**
     * Reason why execution is allowed or blocked.
     */
    private final String reason;

    /**
     * Performs tool approval decision as part of ToolApprovalDecision runtime responsibilities.
     * @param approved The approved used by this operation.
     * @param reason The reason used by this operation.
     */
    private ToolApprovalDecision(boolean approved, String reason)
    {
        this.approved = approved;
        this.reason = reason == null ? "" : reason;
    }

    /**
     * Performs approve as part of ToolApprovalDecision runtime responsibilities.
     * @return The value produced by this operation.
     */
    public static ToolApprovalDecision approve()
    {
        return new ToolApprovalDecision(true, "");
    }

    /**
     * Performs deny as part of ToolApprovalDecision runtime responsibilities.
     * @param reason The reason used by this operation.
     * @return The value produced by this operation.
     */
    public static ToolApprovalDecision deny(String reason)
    {
        return new ToolApprovalDecision(false, Objects.requireNonNull(reason, "reason"));
    }
}
