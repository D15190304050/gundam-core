package stark.dataworks.coderaider.gundam.core.approval;
/**
 * Class AllowAllToolApprovalPolicy.
 */

public class AllowAllToolApprovalPolicy implements ToolApprovalPolicy
{
    /**
     * Executes decide.
     */
    @Override
    public ToolApprovalDecision decide(ToolApprovalRequest request)
    {
        return ToolApprovalDecision.approve();
    }
}
