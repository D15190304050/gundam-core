package stark.dataworks.coderaider.gundam.core.mcp.approval;

/**
 * AllowAllMcpToolApprovalPolicy implements MCP server integration and tool bridging.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class AllowAllMcpToolApprovalPolicy implements McpToolApprovalPolicy
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
