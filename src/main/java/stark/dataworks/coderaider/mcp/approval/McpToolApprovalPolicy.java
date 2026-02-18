package stark.dataworks.coderaider.mcp.approval;

public interface McpToolApprovalPolicy {
    McpToolApprovalResult decide(McpToolApprovalRequest request);
}
