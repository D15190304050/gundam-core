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
 * Run-event listeners that receive streamed events.
     */
    private final List<IRunEventListener> listeners = new ArrayList<>();

    /**
     * Registers a run-event listener.
     * @param listener event listener.
     */
    public void subscribe(IRunEventListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Publishes this value.
     * @param event run event.
     */
    public void publish(RunEvent event)
    {
        listeners.forEach(listener -> listener.onEvent(event));
    }
}
