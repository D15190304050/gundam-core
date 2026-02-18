package stark.dataworks.coderaider.gundam.core.mcp.approval;

public interface McpToolApprovalPolicy {
    McpToolApprovalResult decide(McpToolApprovalRequest request);
}
