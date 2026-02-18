package stark.dataworks.coderaider.gundam.core.mcp.approval;
/**
 * Record McpToolApprovalResult.
 */

public record McpToolApprovalResult(boolean approved, String reason)
{
    public static McpToolApprovalResult approve()
    {
        return new McpToolApprovalResult(true, "");
    }

    public static McpToolApprovalResult deny(String reason)
    {
        return new McpToolApprovalResult(false, reason);
    }
}
