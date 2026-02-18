package stark.dataworks.coderaider.runner;

import stark.dataworks.coderaider.event.RunEvent;

public interface RunHooks {
    default void onEvent(RunEvent event) {
    }
}
