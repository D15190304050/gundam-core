package stark.dataworks.coderaider.gundam.core.mcp;

import java.util.Map;
import java.util.Objects;

/**
 * McpToolDescriptor implements MCP server integration and tool bridging.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class McpToolDescriptor
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
     * Internal state for input schema; used while coordinating runtime behavior.
     */
    private final Map<String, Object> inputSchema;

    /**
     * Performs mcp tool descriptor as part of McpToolDescriptor runtime responsibilities.
     * @param name The name used by this operation.
     * @param description The description used by this operation.
     * @param inputSchema The input schema used by this operation.
     */
    public McpToolDescriptor(String name, String description, Map<String, Object> inputSchema)
    {
        this.name = Objects.requireNonNull(name, "name");
        this.description = description == null ? "" : description;
        this.inputSchema = inputSchema == null ? Map.of() : Map.copyOf(inputSchema);
    }

    /**
     * Returns the current name value maintained by this McpToolDescriptor.
     * @return The value produced by this operation.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the current description value maintained by this McpToolDescriptor.
     * @return The value produced by this operation.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the current input schema value maintained by this McpToolDescriptor.
     * @return The value produced by this operation.
     */
    public Map<String, Object> getInputSchema()
    {
        return inputSchema;
    }
}
