package stark.dataworks.coderaider.genericagent.core.hooks;

import stark.dataworks.coderaider.genericagent.core.runner.AgentRunnerContext;

/**
 * IAgentHook implements runtime lifecycle extension points.
 */
public interface IAgentHook
{

    /**
     * Invoked before an agent run starts.
     *
     * @param context execution context.
     */
    default void beforeRun(AgentRunnerContext context)
    {
    }

    /**
     * Invoked after an agent run finishes.
     *
     * @param context execution context.
     */

    default void afterRun(AgentRunnerContext context)
    {
    }

    /**
     * Invoked after each agent step.
     *
     * @param context execution context.
     */

    default void onStep(AgentRunnerContext context)
    {
    }

    /**
     * Invoked for streamed model text deltas.
     *
     * @param context execution context.
     * @param delta   delta.
     */
    default void onModelResponseDelta(AgentRunnerContext context, String delta)
    {
    }
}
