package stark.dataworks.coderaider.hook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import stark.dataworks.coderaider.runtime.ExecutionContext;

public class HookManager {
    private final List<IAgentHook> agentHooks = new ArrayList<>();
    private final List<IToolHook> toolHooks = new ArrayList<>();

    public void registerAgentHook(IAgentHook hook) {
        agentHooks.add(hook);
    }

    public void registerToolHook(IToolHook hook) {
        toolHooks.add(hook);
    }

    public void beforeRun(ExecutionContext context) {
        agentHooks.forEach(h -> h.beforeRun(context));
    }

    public void onStep(ExecutionContext context) {
        agentHooks.forEach(h -> h.onStep(context));
    }

    public void afterRun(ExecutionContext context) {
        agentHooks.forEach(h -> h.afterRun(context));
    }

    public void beforeTool(String toolName, Map<String, Object> args) {
        toolHooks.forEach(h -> h.beforeTool(toolName, args));
    }

    public void afterTool(String toolName, String result) {
        toolHooks.forEach(h -> h.afterTool(toolName, result));
    }
}
