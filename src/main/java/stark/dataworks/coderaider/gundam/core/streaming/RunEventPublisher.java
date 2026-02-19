package stark.dataworks.coderaider.gundam.core.streaming;

import java.util.ArrayList;
import java.util.List;

import stark.dataworks.coderaider.gundam.core.event.RunEvent;

/**
 * RunEventPublisher implements core runtime responsibilities.
 */
public class RunEventPublisher
{

    /**
     * Internal state for listeners used while coordinating runtime behavior.
     */
    private final List<IRunEventListener> listeners = new ArrayList<>();

    /**
     * Performs subscribe as part of RunEventPublisher runtime responsibilities.
     * @param listener The listener used by this operation.
     */
    public void subscribe(IRunEventListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Publishes a runtime event so hooks/listeners can observe progress.
     * @param event The event used by this operation.
     */
    public void publish(RunEvent event)
    {
        listeners.forEach(listener -> listener.onEvent(event));
    }
}
