package stark.dataworks.coderaider.gundam.core.mcp.approval;

/**
 * AllowAllMcpToolApprovalPolicy implements MCP server integration and tool bridging.
 */
public class AllowAllMcpToolApprovalPolicy implements IMcpToolApprovalPolicy
{

    /**
     * Performs decide as part of AllowAllMcpToolApprovalPolicy runtime responsibilities.
     * @param request The request used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public McpToolApprovalResult decide(McpToolApprovalRequest request)
    {
        return McpToolApprovalResult.approve();
    }
}
