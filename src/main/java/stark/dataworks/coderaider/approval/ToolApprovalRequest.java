package stark.dataworks.coderaider.approval;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ToolApprovalRequest {
    private final String agentId;
    private final String toolName;
    private final Map<String, Object> arguments;

    public ToolApprovalRequest(String agentId, String toolName, Map<String, Object> arguments) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.toolName = Objects.requireNonNull(toolName, "toolName");
        this.arguments = Collections.unmodifiableMap(arguments == null ? Map.of() : arguments);
    }

    public String getAgentId() {
        return agentId;
    }

    public String getToolName() {
        return toolName;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }
}
