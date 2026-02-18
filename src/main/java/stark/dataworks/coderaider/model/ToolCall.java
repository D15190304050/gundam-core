package stark.dataworks.coderaider.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ToolCall {
    private final String toolName;
    private final Map<String, Object> arguments;

    public ToolCall(String toolName, Map<String, Object> arguments) {
        this.toolName = Objects.requireNonNull(toolName, "toolName");
        this.arguments = Collections.unmodifiableMap(Objects.requireNonNull(arguments, "arguments"));
    }

    public String getToolName() {
        return toolName;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }
}
