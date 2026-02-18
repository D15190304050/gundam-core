package stark.dataworks.coderaider.gundam.core.streaming;

import stark.dataworks.coderaider.gundam.core.event.RunEvent;

/**
 * RunEventListener implements core runtime responsibilities.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface RunEventListener
{

    /**
     * Performs on event as part of RunEventListener runtime responsibilities.
     * @param event The event used by this operation.
     */
    void onEvent(RunEvent event);
}
