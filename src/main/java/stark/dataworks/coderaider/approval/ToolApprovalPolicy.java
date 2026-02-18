package stark.dataworks.coderaider.approval;

public interface ToolApprovalPolicy {
    ToolApprovalDecision decide(ToolApprovalRequest request);
}
