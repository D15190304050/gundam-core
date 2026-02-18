package stark.dataworks.coderaider.gundam.core.tool;

import java.util.Map;
/**
 * Interface ITool.
 */

public interface ITool
{
    /**
     * Executes definition.
     */
    ToolDefinition definition();
    /**
     * Executes execute.
     */

    String execute(Map<String, Object> input);
}
