package stark.dataworks.coderaider.gundam.core.hook;

import java.util.Map;
/**
 * Interface IToolHook.
 */

public interface IToolHook
{
    /**
     * Executes beforeTool.
     */
    default void beforeTool(String toolName, Map<String, Object> args)
    {
    }
    /**
     * Executes afterTool.
     */

    default void afterTool(String toolName, String result)
    {
    }
}
