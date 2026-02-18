package stark.dataworks.coderaider.tool;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ToolDefinition {
    private final String name;
    private final String description;
    private final List<ToolParameterSchema> parameters;

    public ToolDefinition(String name, String description, List<ToolParameterSchema> parameters) {
        this.name = Objects.requireNonNull(name, "name");
        this.description = description == null ? "" : description;
        this.parameters = Collections.unmodifiableList(Objects.requireNonNull(parameters, "parameters"));
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ToolParameterSchema> getParameters() {
        return parameters;
    }
}
