package stark.dataworks.coderaider.gundam.core.mcp.approval;

import java.util.Map;

/**
 * McpToolApprovalRequest implements MCP server integration and tool bridging.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public record McpToolApprovalRequest(String serverId, String toolName, Map<String, Object> args)
{
}
