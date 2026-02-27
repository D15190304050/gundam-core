package stark.dataworks.coderaider.gundam.core.tracking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;

/**
 * AgentToolUseTracker tracks which tools each agent has used during a run.
 * This supports model_settings resets based on tool usage history.
 */
public class AgentToolUseTracker
{
    private final Map<String, Set<String>> agentMap;
    private final List<AgentToolRecord> agentToTools;

    public AgentToolUseTracker()
    {
        this.agentMap = new ConcurrentHashMap<>();
        this.agentToTools = Collections.synchronizedList(new ArrayList<>());
    }

    public void recordUsedTools(IAgent agent, List<String> toolNames)
    {
        if (toolNames == null || toolNames.isEmpty())
        {
            return;
        }
        addToolUse(agent, toolNames);
    }

    public void addToolUse(IAgent agent, List<String> toolNames)
    {
        if (agent == null || toolNames == null || toolNames.isEmpty())
        {
            return;
        }

        String agentName = getAgentName(agent);
        Set<String> namesSet = agentMap.computeIfAbsent(agentName, k -> ConcurrentHashMap.newKeySet());
        namesSet.addAll(toolNames);

        synchronized (agentToTools)
        {
            AgentToolRecord existing = null;
            for (AgentToolRecord record : agentToTools)
            {
                if (record.getAgent() == agent)
                {
                    existing = record;
                    break;
                }
            }

            if (existing != null)
            {
                existing.getToolNames().addAll(toolNames);
            }
            else
            {
                agentToTools.add(new AgentToolRecord(agent, new ArrayList<>(toolNames)));
            }
        }
    }

    public boolean hasUsedTools(IAgent agent)
    {
        if (agent == null)
        {
            return false;
        }

        synchronized (agentToTools)
        {
            for (AgentToolRecord record : agentToTools)
            {
                if (record.getAgent() == agent)
                {
                    return !record.getToolNames().isEmpty();
                }
            }
        }
        return false;
    }

    public List<String> getUsedTools(IAgent agent)
    {
        if (agent == null)
        {
            return Collections.emptyList();
        }

        synchronized (agentToTools)
        {
            for (AgentToolRecord record : agentToTools)
            {
                if (record.getAgent() == agent)
                {
                    return Collections.unmodifiableList(new ArrayList<>(record.getToolNames()));
                }
            }
        }
        return Collections.emptyList();
    }

    public Map<String, List<String>> asSerializable()
    {
        Map<String, List<String>> result = new HashMap<>();

        if (!agentMap.isEmpty())
        {
            for (Map.Entry<String, Set<String>> entry : agentMap.entrySet())
            {
                List<String> sorted = new ArrayList<>(entry.getValue());
                Collections.sort(sorted);
                result.put(entry.getKey(), sorted);
            }
            return result;
        }

        Map<String, Set<String>> snapshot = new HashMap<>();
        synchronized (agentToTools)
        {
            for (AgentToolRecord record : agentToTools)
            {
                String agentName = getAgentName(record.getAgent());
                snapshot.computeIfAbsent(agentName, k -> new HashSet<>())
                    .addAll(record.getToolNames());
            }
        }

        for (Map.Entry<String, Set<String>> entry : snapshot.entrySet())
        {
            List<String> sorted = new ArrayList<>(entry.getValue());
            Collections.sort(sorted);
            result.put(entry.getKey(), sorted);
        }

        return result;
    }

    public static AgentToolUseTracker fromSerializable(Map<String, List<String>> data)
    {
        AgentToolUseTracker tracker = new AgentToolUseTracker();
        if (data != null)
        {
            for (Map.Entry<String, List<String>> entry : data.entrySet())
            {
                tracker.agentMap.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
        }
        return tracker;
    }

    public void hydrateFromSnapshot(Map<String, List<String>> snapshot, Map<String, IAgent> agentMap)
    {
        if (snapshot == null || agentMap == null)
        {
            return;
        }

        for (Map.Entry<String, List<String>> entry : snapshot.entrySet())
        {
            IAgent agent = agentMap.get(entry.getKey());
            if (agent != null)
            {
                addToolUse(agent, new ArrayList<>(entry.getValue()));
            }
        }
    }

    public void clear()
    {
        agentMap.clear();
        synchronized (agentToTools)
        {
            agentToTools.clear();
        }
    }

    private String getAgentName(IAgent agent)
    {
        if (agent == null || agent.definition() == null)
        {
            return "unknown";
        }
        String name = agent.definition().getName();
        return name != null ? name : agent.getClass().getSimpleName();
    }

    public static class AgentToolRecord
    {
        private final IAgent agent;
        private final List<String> toolNames;

        public AgentToolRecord(IAgent agent, List<String> toolNames)
        {
            this.agent = agent;
            this.toolNames = Collections.synchronizedList(toolNames != null ? toolNames : new ArrayList<>());
        }

        public IAgent getAgent()
        {
            return agent;
        }

        public List<String> getToolNames()
        {
            return toolNames;
        }
    }
}
