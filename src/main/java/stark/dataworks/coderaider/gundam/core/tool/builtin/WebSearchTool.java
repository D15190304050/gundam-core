package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * WebSearchTool implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class WebSearchTool extends AbstractBuiltinTool
{

    /**
     * Performs web search tool as part of WebSearchTool runtime responsibilities.
     * @param definition The definition used by this operation.
     */
    public WebSearchTool(ToolDefinition definition)
    {
        super(definition, ToolCategory.WEB_SEARCH);
    }

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param input The input used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        return "WebSearch(simulated): " + input.getOrDefault("query", "");
    }
}
