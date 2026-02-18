package stark.dataworks.coderaider.gundam.core.streaming;

import stark.dataworks.coderaider.gundam.core.event.RunEvent;
/**
 * Interface RunEventListener.
 */

public interface RunEventListener
{
    /**
     * Executes onEvent.
     */
    void onEvent(RunEvent event);
}
