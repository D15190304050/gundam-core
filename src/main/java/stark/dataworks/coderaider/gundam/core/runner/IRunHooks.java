package stark.dataworks.coderaider.gundam.core.runner;

import stark.dataworks.coderaider.gundam.core.events.RunEvent;

/**
 * RunHooks implements end-to-end run orchestration including retries, guardrails, handoffs, and events.
 */
public interface IRunHooks
{

    /**
     * Handles a published run event.
     *
     * @param event run event.
     */
    default void onEvent(RunEvent event)
    {
    }
}
