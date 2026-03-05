package stark.dataworks.coderaider.gundam.core.mcp.approval;

/**
 * McpToolApprovalResult implements MCP server integration and tool bridging.
 */
public record McpToolApprovalResult(boolean approved, String reason)
{

    /**
     * Returns an approval decision.
     * @return mcp tool approval result.
     */
    public static McpToolApprovalResult approve()
    {
        return new McpToolApprovalResult(true, "");
    }

    /**
     * Returns a deny decision with a reason.
     * @param reason human-readable reason.
     * @return mcp tool approval result.
     */
    public static McpToolApprovalResult deny(String reason)
    {
        return new McpToolApprovalResult(false, reason);
    }
}
