package stark.dataworks.coderaider.tool;

import java.util.Objects;

public class ToolParameterSchema {
    private final String name;
    private final String type;
    private final boolean required;
    private final String description;

    public ToolParameterSchema(String name, String type, boolean required, String description) {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.required = required;
        this.description = description == null ? "" : description;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDescription() {
        return description;
    }
}
