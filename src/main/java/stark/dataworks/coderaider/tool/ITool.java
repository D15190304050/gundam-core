package stark.dataworks.coderaider.tool;

import java.util.Map;

public interface ITool {
    ToolDefinition definition();

    String execute(Map<String, Object> input);
}
