package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * WebSearchTool implements tool contracts, schema metadata, and executable tool registration.
 */
public class WebSearchTool extends AbstractBuiltinTool
{

    /**
     * Initializes WebSearchTool with required runtime dependencies and options.
     *
     * @param definition definition object.
     */
    public WebSearchTool(ToolDefinition definition)
    {
        super(definition, ToolCategory.WEB_SEARCH);
    }

    /**
     * Executes this tool operation and returns the produced output.
     *
     * @param input input payload.
     * @return Tool execution output returned by the MCP server.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        return "WebSearch(simulated): " + input.getOrDefault("query", "");
    }
}
