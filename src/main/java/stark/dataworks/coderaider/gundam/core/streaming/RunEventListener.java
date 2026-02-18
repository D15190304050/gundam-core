package stark.dataworks.coderaider.gundam.core.streaming;

import stark.dataworks.coderaider.gundam.core.event.RunEvent;

public interface RunEventListener {
    void onEvent(RunEvent event);
}
