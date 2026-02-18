package stark.dataworks.coderaider.gundam.core.mcp;

import java.util.Map;
import java.util.Objects;
/**
 * Class McpToolDescriptor.
 */

public class McpToolDescriptor
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
     * Field inputSchema.
     */
    private final Map<String, Object> inputSchema;
    /**
     * Creates a new McpToolDescriptor instance.
     */

    public McpToolDescriptor(String name, String description, Map<String, Object> inputSchema)
    {
        this.name = Objects.requireNonNull(name, "name");
        this.description = description == null ? "" : description;
        this.inputSchema = inputSchema == null ? Map.of() : Map.copyOf(inputSchema);
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
     * Executes getInputSchema.
     */

    public Map<String, Object> getInputSchema()
    {
        return inputSchema;
    }
}
