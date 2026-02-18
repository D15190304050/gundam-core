package stark.dataworks.coderaider.gundam.core.mcp.approval;

/**
 * McpToolApprovalResult implements MCP server integration and tool bridging.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public record McpToolApprovalResult(boolean approved, String reason)
{

    /**
     * Performs approve as part of McpToolApprovalResult runtime responsibilities.
     * @return The value produced by this operation.
     */
    public static McpToolApprovalResult approve()
    {
        return new McpToolApprovalResult(true, "");
    }

    /**
     * Performs deny as part of McpToolApprovalResult runtime responsibilities.
     * @param reason The reason used by this operation.
     * @return The value produced by this operation.
     */
    public static McpToolApprovalResult deny(String reason)
    {
        return new McpToolApprovalResult(false, reason);
    }
}
