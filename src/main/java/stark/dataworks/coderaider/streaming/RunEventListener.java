package stark.dataworks.coderaider.streaming;

import stark.dataworks.coderaider.event.RunEvent;

public interface RunEventListener {
    void onEvent(RunEvent event);
}
