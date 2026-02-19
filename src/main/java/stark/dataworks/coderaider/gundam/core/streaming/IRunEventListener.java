package stark.dataworks.coderaider.gundam.core.streaming;

import stark.dataworks.coderaider.gundam.core.event.RunEvent;

/**
 * RunEventListener implements core runtime responsibilities.
 */
public interface IRunEventListener
{

    /**
     * Performs on event as part of RunEventListener runtime responsibilities.
     * @param event The event used by this operation.
     */
    void onEvent(RunEvent event);
}
