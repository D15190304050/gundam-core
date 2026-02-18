package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
/**
 * Class WebSearchTool.
 */

public class WebSearchTool extends AbstractBuiltinTool
{
    /**
     * Creates a new WebSearchTool instance.
     */
    public WebSearchTool(ToolDefinition definition)
    {
        super(definition, ToolCategory.WEB_SEARCH);
    }

    /**
     * Executes execute.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        return "WebSearch(simulated): " + input.getOrDefault("query", "");
    }
}
