package stark.dataworks.coderaider.gundam.core.mcp.approval;

/**
 * McpToolApprovalPolicy implements MCP server integration and tool bridging.
 */
public interface IMcpToolApprovalPolicy
{

    /**
     * Performs decide as part of McpToolApprovalPolicy runtime responsibilities.
     * @param request The request used by this operation.
     * @return The value produced by this operation.
     */
    McpToolApprovalResult decide(McpToolApprovalRequest request);
}
