package stark.dataworks.coderaider.gundam.core.tool;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Class ToolRegistry.
 */

public class ToolRegistry implements IToolRegistry
{
    /**
     * Field tools.
     */
    private final Map<String, ITool> tools = new ConcurrentHashMap<>();

    /**
     * Executes register.
     */
    @Override
    public void register(ITool tool)
    {
        tools.put(tool.definition().getName(), tool);
    }

    /**
     * Executes get.
     */
    @Override
    public Optional<ITool> get(String toolName)
    {
        return Optional.ofNullable(tools.get(toolName));
    }
}
