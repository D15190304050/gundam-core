package stark.dataworks.coderaider.gundam.core.approval;

/**
 * AllowAllToolApprovalPolicy implements tool approval workflow.
 */
public class AllowAllToolApprovalPolicy implements IToolApprovalPolicy
{

    /**
     * Evaluates and returns an approval decision.
     * @param request model/tool request payload.
     * @return tool approval decision result.
     */
    @Override
    public ToolApprovalDecision decide(ToolApprovalRequest request)
    {
        return ToolApprovalDecision.approve();
    }
}
