package stark.dataworks.coderaider.gundam.core.tool;

import org.springframework.ai.tool.ToolCallback;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ToolRegistry implements tool contracts, schema metadata, and executable tool registration.
 */
public class ToolRegistry implements IToolRegistry
{

    /**
 * Registered tools keyed by tool name.
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
     * Registers Spring AI tool objects that use {@code @Tool} annotations.
     * @param toolObjects The Spring AI tool objects.
     */
    public void registerSpringToolObjects(Object... toolObjects)
    {
        SpringAiToolAdapters.fromToolObjects(toolObjects).forEach(this::register);
    }

    /**
     * Registers Spring AI {@link ToolCallback} instances.
     * @param callbacks The Spring AI callbacks.
     */
    public void registerSpringToolCallbacks(ToolCallback... callbacks)
    {
        SpringAiToolAdapters.fromToolCallbacks(callbacks).forEach(this::register);
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
