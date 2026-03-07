package stark.dataworks.coderaider.gundam.core.tracking;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.IAgent;

class AgentToolUseTrackerTest
{
    @Test
    void testRecordUsedTools()
    {
        AgentToolUseTracker tracker = new AgentToolUseTracker();
        AgentDefinition def = new AgentDefinition();
        def.setId("test-agent");
        def.setName("TestAgent");
        def.setModel("test-model");
        def.setSystemPrompt("test");
        IAgent agent = def;

        tracker.recordUsedTools(agent, List.of("tool1", "tool2"));

        assertTrue(tracker.hasUsedTools(agent));
        List<String> usedTools = tracker.getUsedTools(agent);
        assertEquals(2, usedTools.size());
        assertTrue(usedTools.contains("tool1"));
        assertTrue(usedTools.contains("tool2"));
    }

    @Test
    void testAddToolUseAccumulates()
    {
        AgentToolUseTracker tracker = new AgentToolUseTracker();
        AgentDefinition def = new AgentDefinition();
        def.setId("test-agent");
        def.setName("TestAgent");
        def.setModel("test-model");
        def.setSystemPrompt("test");
        IAgent agent = def;

        tracker.addToolUse(agent, List.of("tool1"));
        tracker.addToolUse(agent, List.of("tool2"));

        List<String> usedTools = tracker.getUsedTools(agent);
        assertEquals(2, usedTools.size());
        assertTrue(usedTools.contains("tool1"));
        assertTrue(usedTools.contains("tool2"));
    }

    @Test
    void testHasUsedToolsReturnsFalseForUnusedAgent()
    {
        AgentToolUseTracker tracker = new AgentToolUseTracker();
        AgentDefinition def = new AgentDefinition();
        def.setId("test-agent");
        def.setName("TestAgent");
        def.setModel("test-model");
        def.setSystemPrompt("test");
        IAgent agent = def;

        assertFalse(tracker.hasUsedTools(agent));
    }

    @Test
    void testAsSerializable()
    {
        AgentToolUseTracker tracker = new AgentToolUseTracker();
        AgentDefinition def = new AgentDefinition();
        def.setId("test-agent");
        def.setName("TestAgent");
        def.setModel("test-model");
        def.setSystemPrompt("test");
        IAgent agent = def;

        tracker.addToolUse(agent, List.of("toolB", "toolA"));

        Map<String, List<String>> serialized = tracker.asSerializable();
        assertEquals(1, serialized.size());
        assertTrue(serialized.containsKey("TestAgent"));
        List<String> tools = serialized.get("TestAgent");
        assertEquals(2, tools.size());
        assertEquals("toolA", tools.get(0));
        assertEquals("toolB", tools.get(1));
    }

    @Test
    void testFromSerializable()
    {
        Map<String, List<String>> data = Map.of(
            "Agent1", List.of("tool1", "tool2"),
            "Agent2", List.of("tool3")
        );

        AgentToolUseTracker tracker = AgentToolUseTracker.fromSerializable(data);

        Map<String, List<String>> serialized = tracker.asSerializable();
        assertEquals(2, serialized.size());
        assertTrue(serialized.containsKey("Agent1"));
        assertTrue(serialized.containsKey("Agent2"));
    }

    @Test
    void testHydrateFromSnapshot()
    {
        AgentToolUseTracker tracker = new AgentToolUseTracker();
        AgentDefinition def = new AgentDefinition();
        def.setId("test-agent");
        def.setName("TestAgent");
        def.setModel("test-model");
        def.setSystemPrompt("test");
        IAgent agent = def;

        Map<String, List<String>> snapshot = Map.of(
            "TestAgent", List.of("tool1", "tool2")
        );
        Map<String, stark.dataworks.coderaider.gundam.core.agent.IAgent> agentMap = Map.of(
            "TestAgent", agent
        );

        tracker.hydrateFromSnapshot(snapshot, agentMap);

        assertTrue(tracker.hasUsedTools(agent));
        List<String> usedTools = tracker.getUsedTools(agent);
        assertEquals(2, usedTools.size());
    }

    @Test
    void testClear()
    {
        AgentToolUseTracker tracker = new AgentToolUseTracker();
        AgentDefinition def = new AgentDefinition();
        def.setId("test-agent");
        def.setName("TestAgent");
        def.setModel("test-model");
        def.setSystemPrompt("test");
        IAgent agent = def;

        tracker.addToolUse(agent, List.of("tool1"));
        assertTrue(tracker.hasUsedTools(agent));

        tracker.clear();
        assertFalse(tracker.hasUsedTools(agent));
    }

    @Test
    void testMultipleAgents()
    {
        AgentToolUseTracker tracker = new AgentToolUseTracker();

        AgentDefinition def1 = new AgentDefinition();
        def1.setId("agent1");
        def1.setName("Agent1");
        def1.setModel("test-model");
        def1.setSystemPrompt("test");
        IAgent agent1 = def1;

        AgentDefinition def2 = new AgentDefinition();
        def2.setId("agent2");
        def2.setName("Agent2");
        def2.setModel("test-model");
        def2.setSystemPrompt("test");
        IAgent agent2 = def2;

        tracker.addToolUse(agent1, List.of("tool1"));
        tracker.addToolUse(agent2, List.of("tool2", "tool3"));

        assertTrue(tracker.hasUsedTools(agent1));
        assertTrue(tracker.hasUsedTools(agent2));
        assertEquals(1, tracker.getUsedTools(agent1).size());
        assertEquals(2, tracker.getUsedTools(agent2).size());

        Map<String, List<String>> serialized = tracker.asSerializable();
        assertEquals(2, serialized.size());
    }
}
