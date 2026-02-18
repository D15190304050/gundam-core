package stark.dataworks.coderaider.gundam.core.runner;

import stark.dataworks.coderaider.gundam.core.event.RunEvent;

public interface RunHooks {
    default void onEvent(RunEvent event) {
    }
}
