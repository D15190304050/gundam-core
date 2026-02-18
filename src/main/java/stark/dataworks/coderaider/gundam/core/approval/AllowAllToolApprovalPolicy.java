package stark.dataworks.coderaider.gundam.core.approval;

/**
 * AllowAllToolApprovalPolicy implements tool approval workflow.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class AllowAllToolApprovalPolicy implements ToolApprovalPolicy
{

    /**
     * Performs decide as part of AllowAllToolApprovalPolicy runtime responsibilities.
     * @param request The request used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public ToolApprovalDecision decide(ToolApprovalRequest request)
    {
        return ToolApprovalDecision.approve();
    }
}
