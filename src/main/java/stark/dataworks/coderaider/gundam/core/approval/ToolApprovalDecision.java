package stark.dataworks.coderaider.gundam.core.approval;

import java.util.Objects;
/**
 * Class ToolApprovalDecision.
 */

public class ToolApprovalDecision
{
    /**
     * Field approved.
     */
    private final boolean approved;
    /**
     * Field reason.
     */
    private final String reason;
    /**
     * Creates a new ToolApprovalDecision instance.
     */

    private ToolApprovalDecision(boolean approved, String reason)
    {
        this.approved = approved;
        this.reason = reason == null ? "" : reason;
    }
    /**
     * Executes approve.
     */

    public static ToolApprovalDecision approve()
    {
        return new ToolApprovalDecision(true, "");
    }
    /**
     * Executes deny.
     */

    public static ToolApprovalDecision deny(String reason)
    {
        return new ToolApprovalDecision(false, Objects.requireNonNull(reason, "reason"));
    }
    /**
     * Executes isApproved.
     */

    public boolean isApproved()
    {
        return approved;
    }
    /**
     * Executes getReason.
     */

    public String getReason()
    {
        return reason;
    }
}
