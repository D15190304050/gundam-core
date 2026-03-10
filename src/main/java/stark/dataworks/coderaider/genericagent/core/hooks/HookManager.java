package stark.dataworks.coderaider.genericagent.core.hooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.genericagent.core.runner.AgentRunnerContext;

/**
 * HookManager implements runtime lifecycle extension points.
 */
public class HookManager
{

    /**
     * Registered agent lifecycle hooks.
     */
    private final List<IAgentHook> agentHooks = new ArrayList<>();

    /**
     * Registered tool lifecycle hooks.
     */
    private final List<IToolHook> toolHooks = new ArrayList<>();

    /**
     * Registers an agent lifecycle hook.
     *
     * @param hook hook.
     */
    public void registerAgentHook(IAgentHook hook)
    {
        agentHooks.add(hook);
    }

    /**
     * Registers a tool lifecycle hook.
     *
     * @param hook hook.
     */
    public void registerToolHook(IToolHook hook)
    {
        toolHooks.add(hook);
    }

    /**
     * Invoked before an agent run starts.
     *
     * @param context execution context.
     */
    public void beforeRun(AgentRunnerContext context)
    {
        agentHooks.forEach(h -> h.beforeRun(context));
    }

    /**
     * Invoked after each agent step.
     *
     * @param context execution context.
     */
    public void onStep(AgentRunnerContext context)
    {
        agentHooks.forEach(h -> h.onStep(context));
    }

    /**
     * Invoked for streamed model text deltas.
     *
     * @param context execution context.
     * @param delta   delta.
     */
    public void onModelResponseDelta(AgentRunnerContext context, String delta)
    {
        agentHooks.forEach(h -> h.onModelResponseDelta(context, delta));
    }

    /**
     * Invoked after an agent run finishes.
     *
     * @param context execution context.
     */
    public void afterRun(AgentRunnerContext context)
    {
        agentHooks.forEach(h -> h.afterRun(context));
    }

    /**
     * Invoked before a tool call executes.
     *
     * @param toolName tool name.
     * @param args     tool arguments passed to the MCP server.
     */
    public void beforeTool(String toolName, Map<String, Object> args)
    {
        toolHooks.forEach(h -> h.beforeTool(toolName, args));
    }

    /**
     * Invoked after a tool call completes.
     *
     * @param toolName tool name.
     * @param result   result.
     */
    public void afterTool(String toolName, String result)
    {
        toolHooks.forEach(h -> h.afterTool(toolName, result));
    }
}
