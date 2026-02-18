package stark.dataworks.coderaider.gundam.core.hook;

import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;

public interface IAgentHook {
    default void beforeRun(ExecutionContext context) {}

    default void afterRun(ExecutionContext context) {}

    default void onStep(ExecutionContext context) {}
}
