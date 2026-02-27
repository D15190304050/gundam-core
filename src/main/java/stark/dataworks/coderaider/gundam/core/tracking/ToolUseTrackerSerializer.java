package stark.dataworks.coderaider.gundam.core.tracking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;

/**
 * Utility methods for serializing and hydrating AgentToolUseTracker state.
 */
public final class ToolUseTrackerSerializer
{
    private ToolUseTrackerSerializer()
    {
    }

    public static Map<String, List<String>> serialize(AgentToolUseTracker tracker)
    {
        if (tracker == null)
        {
            return new HashMap<>();
        }
        return tracker.asSerializable();
    }

    public static AgentToolUseTracker deserialize(Map<String, List<String>> data)
    {
        return AgentToolUseTracker.fromSerializable(data);
    }

    public static void hydrate(AgentToolUseTracker tracker, Map<String, List<String>> snapshot, Map<String, IAgent> agentMap)
    {
        if (tracker == null || snapshot == null || agentMap == null)
        {
            return;
        }
        tracker.hydrateFromSnapshot(snapshot, agentMap);
    }

    public static Map<String, IAgent> buildAgentMap(IAgent startingAgent)
    {
        Map<String, IAgent> agentMap = new HashMap<>();
        if (startingAgent != null && startingAgent.definition() != null)
        {
            agentMap.put(startingAgent.definition().getName(), startingAgent);
        }
        return agentMap;
    }
}
