package stark.dataworks.coderaider.gundam.core.mcp.approval;

/**
 * McpToolApprovalPolicy implements MCP server integration and tool bridging.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface McpToolApprovalPolicy
{

    /**
     * Performs decide as part of McpToolApprovalPolicy runtime responsibilities.
     * @param request The request used by this operation.
     * @return The value produced by this operation.
     */
    McpToolApprovalResult decide(McpToolApprovalRequest request);
}
