package stark.dataworks.coderaider.gundam.core.hook;

import java.util.Map;

/**
 * IToolHook implements runtime lifecycle extension points.
 */
public interface IToolHook
{

    /**
     * Invoked before a tool call executes.
     * @param toolName tool name.
     * @param Map<String map<string.
     * @param args tool arguments passed to the MCP server.
     */
    default void beforeTool(String toolName, Map<String, Object> args)
    {
    }

    /**
     * Invoked after a tool call completes.
     * @param toolName tool name.
     * @param result result.
     */

    default void afterTool(String toolName, String result)
    {
    }
}
