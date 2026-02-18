package stark.dataworks.coderaider.gundam.core.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
/**
 * Class ToolCall.
 */

public class ToolCall
{
    /**
     * Field toolName.
     */
    private final String toolName;
    /**
     * Field arguments.
     */
    private final Map<String, Object> arguments;
    /**
     * Creates a new ToolCall instance.
     */

    public ToolCall(String toolName, Map<String, Object> arguments)
    {
        this.toolName = Objects.requireNonNull(toolName, "toolName");
        this.arguments = Collections.unmodifiableMap(Objects.requireNonNull(arguments, "arguments"));
    }
    /**
     * Executes getToolName.
     */

    public String getToolName()
    {
        return toolName;
    }
    /**
     * Executes getArguments.
     */

    public Map<String, Object> getArguments()
    {
        return arguments;
    }
}
