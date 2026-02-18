package stark.dataworks.coderaider.tool.builtin;

import java.util.Map;
import stark.dataworks.coderaider.tool.ToolCategory;
import stark.dataworks.coderaider.tool.ToolDefinition;

public class WebSearchTool extends AbstractBuiltinTool {
    public WebSearchTool(ToolDefinition definition) {
        super(definition, ToolCategory.WEB_SEARCH);
    }

    @Override
    public String execute(Map<String, Object> input) {
        return "WebSearch(simulated): " + input.getOrDefault("query", "");
    }
}
