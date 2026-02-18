package stark.dataworks.coderaider.gundam.core.mcp.approval;

public class AllowAllMcpToolApprovalPolicy implements McpToolApprovalPolicy {
    @Override
    public McpToolApprovalResult decide(McpToolApprovalRequest request) {
        return McpToolApprovalResult.approve();
    }
}
