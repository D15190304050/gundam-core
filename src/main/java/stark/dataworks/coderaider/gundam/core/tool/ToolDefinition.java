package stark.dataworks.coderaider.gundam.core.tool;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ToolDefinition implements tool contracts, schema metadata, and executable tool registration.
 */
@Getter
public class ToolDefinition
{

    /**
     * Human-readable name used in logs and UIs.
     */
    private final String name;

    /**
     * Human-readable description shown to model and operators.
     */
    private final String description;

    /**
     * JSON-schema-like parameter definitions for tool inputs.
     */
    private final List<ToolParameterSchema> parameters;

    /**
     * Performs tool definition as part of ToolDefinition runtime responsibilities.
     * @param name The name used by this operation.
     * @param description The description used by this operation.
     * @param parameters The parameters used by this operation.
     */
    public ToolDefinition(String name, String description, List<ToolParameterSchema> parameters)
    {
        this.name = Objects.requireNonNull(name, "name");
        this.description = description == null ? "" : description;
        this.parameters = Collections.unmodifiableList(Objects.requireNonNull(parameters, "parameters"));
    }
}
