package stark.dataworks.coderaider.gundam.core.tool;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ToolDefinition implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class ToolDefinition
{

    /**
     * Internal state for name; used while coordinating runtime behavior.
     */
    private final String name;

    /**
     * Internal state for description; used while coordinating runtime behavior.
     */
    private final String description;

    /**
     * Internal state for parameters; used while coordinating runtime behavior.
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

    /**
     * Returns the current name value maintained by this ToolDefinition.
     * @return The value produced by this operation.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the current description value maintained by this ToolDefinition.
     * @return The value produced by this operation.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the current parameters value maintained by this ToolDefinition.
     * @return The value produced by this operation.
     */
    public List<ToolParameterSchema> getParameters()
    {
        return parameters;
    }
}
