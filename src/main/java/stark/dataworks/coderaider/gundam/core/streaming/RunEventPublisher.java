package stark.dataworks.coderaider.gundam.core.streaming;

import java.util.ArrayList;
import java.util.List;

import stark.dataworks.coderaider.gundam.core.event.RunEvent;
/**
 * Class RunEventPublisher.
 */

public class RunEventPublisher
{
    /**
     * Field listeners.
     */
    private final List<RunEventListener> listeners = new ArrayList<>();
    /**
     * Executes subscribe.
     */

    public void subscribe(RunEventListener listener)
    {
        listeners.add(listener);
    }
    /**
     * Executes publish.
     */

    public void publish(RunEvent event)
    {
        listeners.forEach(listener -> listener.onEvent(event));
    }
}
