package stark.dataworks.coderaider.gundam.core.tool;

import java.util.Map;

public interface ITool {
    ToolDefinition definition();

    String execute(Map<String, Object> input);
}
