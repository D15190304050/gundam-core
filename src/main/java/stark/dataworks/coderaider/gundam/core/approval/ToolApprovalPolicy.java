package stark.dataworks.coderaider.gundam.core.approval;
/**
 * Interface ToolApprovalPolicy.
 */

public interface ToolApprovalPolicy
{
    /**
     * Executes decide.
     */
    ToolApprovalDecision decide(ToolApprovalRequest request);
}
