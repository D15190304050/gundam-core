package stark.dataworks.coderaider.gundam.core.tool;

import lombok.Getter;

import java.util.Objects;

/**
 * ToolParameterSchema implements tool contracts, schema metadata, and executable tool registration.
 */
@Getter
public class ToolParameterSchema
{

    /**
     * Human-readable name used in logs and UIs.
     */
    private final String name;

    /**
     * Type discriminator for this item/event/span.
     */
    private final String type;

    /**
     * Whether this tool parameter is mandatory.
     */
    private final boolean required;

    /**
     * Human-readable description shown to model and operators.
     */
    private final String description;

    /**
     * Performs tool parameter schema as part of ToolParameterSchema runtime responsibilities.
     * @param name The name used by this operation.
     * @param type The type used by this operation.
     * @param required The required used by this operation.
     * @param description The description used by this operation.
     */
    public ToolParameterSchema(String name, String type, boolean required, String description)
    {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.required = required;
        this.description = description == null ? "" : description;
    }
}
