package stark.dataworks.coderaider.gundam.core.mcp.approval;

import java.util.Map;
/**
 * Record McpToolApprovalRequest.
 */

public record McpToolApprovalRequest(String serverId, String toolName, Map<String, Object> args)
{
}
