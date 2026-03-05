package stark.dataworks.coderaider.gundam.core.approval;

/**
 * ToolApprovalPolicy implements tool approval workflow.
 */
public interface IToolApprovalPolicy
{

    /**
     * Evaluates and returns an approval decision.
     * @param request request payload.
     * @return tool approval decision result.
     */
    ToolApprovalDecision decide(ToolApprovalRequest request);
}
