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
     * Initializes ToolApprovalDecision with required runtime dependencies and options.
     * @param approved approved.
     * @param reason human-readable reason.
     */
    private ToolApprovalDecision(boolean approved, String reason)
    {
        this.approved = approved;
        this.reason = reason == null ? "" : reason;
    }

    /**
     * Returns an approval decision.
     * @return tool approval decision result.
     */
    public static ToolApprovalDecision approve()
    {
        return new ToolApprovalDecision(true, "");
    }

    /**
     * Returns a deny decision with a reason.
     * @param reason human-readable reason.
     * @return tool approval decision result.
     */
    public static ToolApprovalDecision deny(String reason)
    {
        return new ToolApprovalDecision(false, Objects.requireNonNull(reason, "reason"));
    }
}
