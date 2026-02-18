package stark.dataworks.coderaider.gundam.core.runner;

import stark.dataworks.coderaider.gundam.core.event.RunEvent;
/**
 * Interface RunHooks.
 */

public interface RunHooks
{
    /**
     * Executes onEvent.
     */
    default void onEvent(RunEvent event)
    {
    }
}
