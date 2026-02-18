package stark.dataworks.coderaider.gundam.core.tool;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ToolRegistry implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class ToolRegistry implements IToolRegistry
{

    /**
     * Internal state for tools used while coordinating runtime behavior.
     */
    private final Map<String, ITool> tools = new ConcurrentHashMap<>();

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param tool The tool used by this operation.
     */
    @Override
    public void register(ITool tool)
    {
        tools.put(tool.definition().getName(), tool);
    }

    /**
     * Returns the value requested by the caller from this ToolRegistry.
     * @param toolName The tool name used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public Optional<ITool> get(String toolName)
    {
        return Optional.ofNullable(tools.get(toolName));
    }
}
