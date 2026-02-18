package stark.dataworks.coderaider.tool;

import java.util.Map;

public class ToolExecutionContext {
    private final String agentId;
    private final Map<String, Object> runtimeMetadata;

    public ToolExecutionContext(String agentId, Map<String, Object> runtimeMetadata) {
        this.agentId = agentId;
        this.runtimeMetadata = runtimeMetadata == null ? Map.of() : Map.copyOf(runtimeMetadata);
    }

    public String getAgentId() {
        return agentId;
    }

    public Map<String, Object> getRuntimeMetadata() {
        return runtimeMetadata;
    }
}
