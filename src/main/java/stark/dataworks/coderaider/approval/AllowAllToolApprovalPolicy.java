package stark.dataworks.coderaider.approval;

public class AllowAllToolApprovalPolicy implements ToolApprovalPolicy {
    @Override
    public ToolApprovalDecision decide(ToolApprovalRequest request) {
        return ToolApprovalDecision.approve();
    }
}
