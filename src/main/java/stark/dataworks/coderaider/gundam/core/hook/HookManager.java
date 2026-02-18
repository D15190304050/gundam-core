package stark.dataworks.coderaider.gundam.core.hook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;
/**
 * Class HookManager.
 */

public class HookManager
{
    /**
     * Field agentHooks.
     */
    private final List<IAgentHook> agentHooks = new ArrayList<>();
    /**
     * Field toolHooks.
     */
    private final List<IToolHook> toolHooks = new ArrayList<>();
    /**
     * Executes registerAgentHook.
     */

    public void registerAgentHook(IAgentHook hook)
    {
        agentHooks.add(hook);
    }
    /**
     * Executes registerToolHook.
     */

    public void registerToolHook(IToolHook hook)
    {
        toolHooks.add(hook);
    }
    /**
     * Executes beforeRun.
     */

    public void beforeRun(ExecutionContext context)
    {
        agentHooks.forEach(h -> h.beforeRun(context));
    }
    /**
     * Executes onStep.
     */

    public void onStep(ExecutionContext context)
    {
        agentHooks.forEach(h -> h.onStep(context));
    }
    /**
     * Executes afterRun.
     */

    public void afterRun(ExecutionContext context)
    {
        agentHooks.forEach(h -> h.afterRun(context));
    }
    /**
     * Executes beforeTool.
     */

    public void beforeTool(String toolName, Map<String, Object> args)
    {
        toolHooks.forEach(h -> h.beforeTool(toolName, args));
    }
    /**
     * Executes afterTool.
     */

    public void afterTool(String toolName, String result)
    {
        toolHooks.forEach(h -> h.afterTool(toolName, result));
    }
}
