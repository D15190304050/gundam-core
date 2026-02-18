package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
/**
 * Class FileSearchTool.
 */

public class FileSearchTool extends AbstractBuiltinTool
{
    /**
     * Creates a new FileSearchTool instance.
     */
    public FileSearchTool(ToolDefinition definition)
    {
        super(definition, ToolCategory.FILE_SEARCH);
    }

    /**
     * Executes execute.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        return "FileSearch(simulated): " + input.getOrDefault("path", "") + " q=" + input.getOrDefault("query", "");
    }
}
