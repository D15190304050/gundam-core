package stark.dataworks.coderaider.gundam.core.mcp.approval;
/**
 * Interface McpToolApprovalPolicy.
 */

public interface McpToolApprovalPolicy
{
    /**
     * Executes decide.
     */
    McpToolApprovalResult decide(McpToolApprovalRequest request);
}
