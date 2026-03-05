package stark.dataworks.coderaider.gundam.core.hook;

import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;

/**
 * IAgentHook implements runtime lifecycle extension points.
 */
public interface IAgentHook
{

    /**
     * Invoked before an agent run starts.
     * @param context execution context.
     */
    default void beforeRun(ExecutionContext context)
    {
    }

    /**
     * Invoked after an agent run finishes.
     * @param context execution context.
     */

    default void afterRun(ExecutionContext context)
    {
    }

    /**
     * Invoked after each agent step.
     * @param context execution context.
     */

    default void onStep(ExecutionContext context)
    {
    }

    /**
     * Invoked for streamed model text deltas.
     * @param context execution context.
     * @param delta delta.
     */
    default void onModelResponseDelta(ExecutionContext context, String delta)
    {
    }
}
