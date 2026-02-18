package stark.dataworks.coderaider.gundam.core.mcp.approval;
/**
 * Class AllowAllMcpToolApprovalPolicy.
 */

public class AllowAllMcpToolApprovalPolicy implements McpToolApprovalPolicy
{
    /**
     * Executes decide.
     */
    @Override
    public McpToolApprovalResult decide(McpToolApprovalRequest request)
    {
        return McpToolApprovalResult.approve();
    }
}
