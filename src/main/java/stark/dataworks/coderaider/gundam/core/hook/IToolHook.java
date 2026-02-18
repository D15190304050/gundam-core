package stark.dataworks.coderaider.gundam.core.hook;

import java.util.Map;

/**
 * IToolHook implements runtime lifecycle extension points.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface IToolHook
{

    /**
     * Performs before tool as part of IToolHook runtime responsibilities.
     * @param toolName The tool name used by this operation.
     * @param args The args used by this operation.
     */
    default void beforeTool(String toolName, Map<String, Object> args)
    {
    }

    /**
     * Performs after tool as part of IToolHook runtime responsibilities.
     * @param toolName The tool name used by this operation.
     * @param result The result used by this operation.
     */

    default void afterTool(String toolName, String result)
    {
    }
}
