package stark.dataworks.coderaider.hook;

import stark.dataworks.coderaider.runtime.ExecutionContext;

public interface IAgentHook {
    default void beforeRun(ExecutionContext context) {}

    default void afterRun(ExecutionContext context) {}

    default void onStep(ExecutionContext context) {}
}
