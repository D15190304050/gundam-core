package stark.dataworks.coderaider.mcp.approval;

import java.util.Map;

public record McpToolApprovalRequest(String serverId, String toolName, Map<String, Object> args) {
}
