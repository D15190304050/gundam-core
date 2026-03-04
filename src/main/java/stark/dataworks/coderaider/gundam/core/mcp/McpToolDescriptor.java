package stark.dataworks.coderaider.gundam.core.mcp;

import lombok.Getter;

import java.util.Map;
import java.util.Objects;

/**
 * McpToolDescriptor implements MCP server integration and tool bridging.
 */
@Getter
public class McpToolDescriptor
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
     * Raw MCP input schema used to build local tool parameters.
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
}
