package stark.dataworks.coderaider.gundam.core.tool;

import java.util.Objects;
/**
 * Class ToolParameterSchema.
 */

public class ToolParameterSchema
{
    /**
     * Field name.
     */
    private final String name;
    /**
     * Field type.
     */
    private final String type;
    /**
     * Field required.
     */
    private final boolean required;
    /**
     * Field description.
     */
    private final String description;
    /**
     * Creates a new ToolParameterSchema instance.
     */

    public ToolParameterSchema(String name, String type, boolean required, String description)
    {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.required = required;
        this.description = description == null ? "" : description;
    }
    /**
     * Executes getName.
     */

    public String getName()
    {
        return name;
    }
    /**
     * Executes getType.
     */

    public String getType()
    {
        return type;
    }
    /**
     * Executes isRequired.
     */

    public boolean isRequired()
    {
        return required;
    }
    /**
     * Executes getDescription.
     */

    public String getDescription()
    {
        return description;
    }
}
