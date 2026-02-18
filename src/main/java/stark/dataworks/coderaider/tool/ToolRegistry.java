package stark.dataworks.coderaider.tool;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ToolRegistry implements IToolRegistry {
    private final Map<String, ITool> tools = new ConcurrentHashMap<>();

    @Override
    public void register(ITool tool) {
        tools.put(tool.definition().getName(), tool);
    }

    @Override
    public Optional<ITool> get(String toolName) {
        return Optional.ofNullable(tools.get(toolName));
    }
}
