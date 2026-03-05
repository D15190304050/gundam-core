package stark.dataworks.coderaider.gundam.core.mcp.approval;

/**
 * McpToolApprovalPolicy implements MCP server integration and tool bridging.
 */
public interface IMcpToolApprovalPolicy
{

    /**
     * Evaluates and returns an approval decision.
     * @param request request payload.
     * @return mcp tool approval result.
     */
    McpToolApprovalResult decide(McpToolApprovalRequest request);
}
