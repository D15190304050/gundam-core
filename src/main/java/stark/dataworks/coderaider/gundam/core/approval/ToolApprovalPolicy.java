package stark.dataworks.coderaider.gundam.core.approval;

public interface ToolApprovalPolicy {
    ToolApprovalDecision decide(ToolApprovalRequest request);
}
