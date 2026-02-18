package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * FileSearchTool implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class FileSearchTool extends AbstractBuiltinTool
{

    /**
     * Performs file search tool as part of FileSearchTool runtime responsibilities.
     * @param definition The definition used by this operation.
     */
    public FileSearchTool(ToolDefinition definition)
    {
        super(definition, ToolCategory.FILE_SEARCH);
    }

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param input The input used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        return "FileSearch(simulated): " + input.getOrDefault("path", "") + " q=" + input.getOrDefault("query", "");
    }
}
