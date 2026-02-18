package stark.dataworks.coderaider.gundam.core.tool;

import lombok.Getter;

import java.util.Objects;

/**
 * ToolParameterSchema implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
@Getter
public class ToolParameterSchema
{

    /**
     * Internal state for name; used while coordinating runtime behavior.
     */
    private final String name;

    /**
     * Internal state for type; used while coordinating runtime behavior.
     */
    private final String type;

    /**
     * Internal state for required; used while coordinating runtime behavior.
     */
    private final boolean required;

    /**
     * Internal state for description; used while coordinating runtime behavior.
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
