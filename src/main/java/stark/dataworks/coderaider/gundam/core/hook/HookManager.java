package stark.dataworks.coderaider.gundam.core.hook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;

/**
 * HookManager implements runtime lifecycle extension points.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class HookManager
{

    /**
     * Internal state for agent hooks used while coordinating runtime behavior.
     */
    private final List<IAgentHook> agentHooks = new ArrayList<>();

    /**
     * Internal state for tool hooks used while coordinating runtime behavior.
     */
    private final List<IToolHook> toolHooks = new ArrayList<>();

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param hook The hook used by this operation.
     */
    public void registerAgentHook(IAgentHook hook)
    {
        agentHooks.add(hook);
    }

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param hook The hook used by this operation.
     */
    public void registerToolHook(IToolHook hook)
    {
        toolHooks.add(hook);
    }

    /**
     * Performs before run as part of HookManager runtime responsibilities.
     * @param context The context used by this operation.
     */
    public void beforeRun(ExecutionContext context)
    {
        agentHooks.forEach(h -> h.beforeRun(context));
    }

    /**
     * Performs on step as part of HookManager runtime responsibilities.
     * @param context The context used by this operation.
     */
    public void onStep(ExecutionContext context)
    {
        agentHooks.forEach(h -> h.onStep(context));
    }

    /**
     * Performs after run as part of HookManager runtime responsibilities.
     * @param context The context used by this operation.
     */
    public void afterRun(ExecutionContext context)
    {
        agentHooks.forEach(h -> h.afterRun(context));
    }

    /**
     * Performs before tool as part of HookManager runtime responsibilities.
     * @param toolName The tool name used by this operation.
     * @param args The args used by this operation.
     */
    public void beforeTool(String toolName, Map<String, Object> args)
    {
        toolHooks.forEach(h -> h.beforeTool(toolName, args));
    }

    /**
     * Performs after tool as part of HookManager runtime responsibilities.
     * @param toolName The tool name used by this operation.
     * @param result The result used by this operation.
     */
    public void afterTool(String toolName, String result)
    {
        toolHooks.forEach(h -> h.afterTool(toolName, result));
    }
}
