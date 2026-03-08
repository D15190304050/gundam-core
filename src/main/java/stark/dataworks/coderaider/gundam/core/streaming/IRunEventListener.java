package stark.dataworks.coderaider.gundam.core.streaming;

import stark.dataworks.coderaider.gundam.core.events.RunEvent;

/**
 * RunEventListener implements core runtime responsibilities.
 */
public interface IRunEventListener
{

    /**
     * Handles a published run event.
     *
     * @param event run event.
     */
    void onEvent(RunEvent event);
}
