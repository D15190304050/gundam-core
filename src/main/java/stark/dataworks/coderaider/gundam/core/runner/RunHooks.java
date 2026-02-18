package stark.dataworks.coderaider.gundam.core.runner;

import stark.dataworks.coderaider.gundam.core.event.RunEvent;

/**
 * RunHooks implements end-to-end run orchestration including retries, guardrails, handoffs, and events.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface RunHooks
{

    /**
     * Performs on event as part of RunHooks runtime responsibilities.
     * @param event The event used by this operation.
     */
    default void onEvent(RunEvent event)
    {
    }
}
