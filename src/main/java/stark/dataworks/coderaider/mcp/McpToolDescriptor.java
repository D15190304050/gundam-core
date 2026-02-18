package stark.dataworks.coderaider.mcp;

import java.util.Map;
import java.util.Objects;

public class McpToolDescriptor {
    private final String name;
    private final String description;
    private final Map<String, Object> inputSchema;

    public McpToolDescriptor(String name, String description, Map<String, Object> inputSchema) {
        this.name = Objects.requireNonNull(name, "name");
        this.description = description == null ? "" : description;
        this.inputSchema = inputSchema == null ? Map.of() : Map.copyOf(inputSchema);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }
}
