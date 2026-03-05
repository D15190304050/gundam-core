package stark.dataworks.coderaider.gundam.core.mcp.approval;

/**
 * AllowAllMcpToolApprovalPolicy implements MCP server integration and tool bridging.
 */
public class AllowAllMcpToolApprovalPolicy implements IMcpToolApprovalPolicy
{

    /**
     * Evaluates and returns an approval decision.
     * @param request model/tool request payload.
     * @return mcp tool approval result.
     */
    @Override
    public McpToolApprovalResult decide(McpToolApprovalRequest request)
    {
        return McpToolApprovalResult.approve();
    }
}
