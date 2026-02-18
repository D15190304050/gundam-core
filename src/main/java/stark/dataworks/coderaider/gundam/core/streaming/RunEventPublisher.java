package stark.dataworks.coderaider.gundam.core.streaming;

import java.util.ArrayList;
import java.util.List;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;

public class RunEventPublisher {
    private final List<RunEventListener> listeners = new ArrayList<>();

    public void subscribe(RunEventListener listener) {
        listeners.add(listener);
    }

    public void publish(RunEvent event) {
        listeners.forEach(listener -> listener.onEvent(event));
    }
}
