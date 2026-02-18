package stark.dataworks.coderaider.gundam.core.tool;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * Class ToolDefinition.
 */

public class ToolDefinition
{
    /**
     * Field name.
     */
    private final String name;
    /**
     * Field description.
     */
    private final String description;
    /**
     * Field parameters.
     */
    private final List<ToolParameterSchema> parameters;
    /**
     * Creates a new ToolDefinition instance.
     */

    public ToolDefinition(String name, String description, List<ToolParameterSchema> parameters)
    {
        this.name = Objects.requireNonNull(name, "name");
        this.description = description == null ? "" : description;
        this.parameters = Collections.unmodifiableList(Objects.requireNonNull(parameters, "parameters"));
    }
    /**
     * Executes getName.
     */

    public String getName()
    {
        return name;
    }
    /**
     * Executes getDescription.
     */

    public String getDescription()
    {
        return description;
    }
    /**
     * Executes getParameters.
     */

    public List<ToolParameterSchema> getParameters()
    {
        return parameters;
    }
}
