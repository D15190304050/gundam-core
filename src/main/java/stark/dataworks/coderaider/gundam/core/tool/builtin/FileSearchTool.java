package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;
import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

public class FileSearchTool extends AbstractBuiltinTool {
    public FileSearchTool(ToolDefinition definition) {
        super(definition, ToolCategory.FILE_SEARCH);
    }

    @Override
    public String execute(Map<String, Object> input) {
        return "FileSearch(simulated): " + input.getOrDefault("path", "") + " q=" + input.getOrDefault("query", "");
    }
}
