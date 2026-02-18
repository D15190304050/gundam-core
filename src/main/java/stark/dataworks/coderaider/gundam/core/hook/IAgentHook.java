package stark.dataworks.coderaider.gundam.core.hook;

import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;
/**
 * Interface IAgentHook.
 */

public interface IAgentHook
{
    /**
     * Executes beforeRun.
     */
    default void beforeRun(ExecutionContext context)
    {
    }
    /**
     * Executes afterRun.
     */

    default void afterRun(ExecutionContext context)
    {
    }
    /**
     * Executes onStep.
     */

    default void onStep(ExecutionContext context)
    {
    }
}
