package stark.dataworks.coderaider.gundam.core.realtime.transport;

import java.util.ArrayList;
import java.util.List;

import stark.dataworks.coderaider.gundam.core.realtime.RealtimeEvent;

/**
 * In-process SSE-like adapter that forwards encoded events to listeners.
 */
public class SseRealtimeEventTransport implements IRealtimeEventTransport
{
    public interface Listener
    {
        void onData(String payload);
    }

    private final List<Listener> listeners = new ArrayList<>();

    public void subscribe(Listener listener)
    {
        listeners.add(listener);
    }

    @Override
    public void publish(RealtimeEvent event)
    {
        String payload = "event: " + event.getType().name().toLowerCase() + "\n" +
            "data: " + event.getData() + "\n\n";
        listeners.forEach(listener -> listener.onData(payload));
    }
}
