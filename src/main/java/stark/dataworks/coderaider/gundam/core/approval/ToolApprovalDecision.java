package stark.dataworks.coderaider.gundam.core.approval;

import java.util.Objects;

/**
 * ToolApprovalDecision implements tool approval workflow.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class ToolApprovalDecision
{

    /**
     * Internal state for approved; used while coordinating runtime behavior.
     */
    private final boolean approved;

    /**
     * Internal state for reason; used while coordinating runtime behavior.
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

    /**
     * Reports whether approved is currently satisfied.
     * @return {@code true} when the condition is satisfied; otherwise {@code false}.
     */
    public boolean isApproved()
    {
        return approved;
    }

    /**
     * Returns the current reason value maintained by this ToolApprovalDecision.
     * @return The value produced by this operation.
     */
    public String getReason()
    {
        return reason;
    }
}
