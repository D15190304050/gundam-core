package stark.dataworks.coderaider.streaming;

import java.util.ArrayList;
import java.util.List;
import stark.dataworks.coderaider.event.RunEvent;

public class RunEventPublisher {
    private final List<RunEventListener> listeners = new ArrayList<>();

    public void subscribe(RunEventListener listener) {
        listeners.add(listener);
    }

    public void publish(RunEvent event) {
        listeners.forEach(listener -> listener.onEvent(event));
    }
}
