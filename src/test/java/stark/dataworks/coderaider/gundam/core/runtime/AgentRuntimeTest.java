package stark.dataworks.coderaider.gundam.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinitionLoader;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.context.DefaultContextBuilder;
import stark.dataworks.coderaider.gundam.core.hook.HookManager;
import stark.dataworks.coderaider.gundam.core.hook.IAgentHook;
import stark.dataworks.coderaider.gundam.core.hook.IToolHook;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmStreamListener;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
import stark.dataworks.coderaider.gundam.core.runtime.AgentRunResult;
import stark.dataworks.coderaider.gundam.core.runtime.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runtime.DefaultStepEngine;
import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

class AgentRuntimeTest
{

    @Test
    void executesToolCallThenReturnsFinalMessageAndTracksTokens()
    {
        AgentDefinition definition = definition("planner", "weather-model", List.of("weather"));
        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(definition));

        ToolRegistry tools = new ToolRegistry();
        tools.register(new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition("weather", "get weather", List.of(new ToolParameterSchema("city", "string", true, "")));
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                return "Sunny in " + input.get("city");
            }
        });

        Queue<LlmResponse> responses = new ArrayDeque<>();
        responses.add(new LlmResponse("", List.of(new ToolCall("weather", Map.of("city", "Tokyo"))), null, new TokenUsage(10, 3)));
        responses.add(new LlmResponse("Final forecast delivered", List.of(), null, new TokenUsage(5, 7)));
        ILlmClient scripted = request -> responses.remove();

        HookManager hookManager = new HookManager();
        AtomicInteger beforeToolCalls = new AtomicInteger();
        hookManager.registerToolHook(new IToolHook()
        {
            @Override
            public void beforeTool(String toolName, Map<String, Object> args)
            {
                beforeToolCalls.incrementAndGet();
            }
        });

        AgentRunner runner = new AgentRunner(new DefaultStepEngine(scripted, tools, agentRegistry, new DefaultContextBuilder(), hookManager));

        AgentRunResult result = runner.run(new Agent(definition), "What's weather?");

        assertEquals("Final forecast delivered", result.getOutput());
        assertEquals(25, result.getUsage().getTotalTokens());
        assertEquals(1, beforeToolCalls.get());
    }

    @Test
    void supportsAgentHandoff()
    {
        AgentDefinition planner = definition("planner", "model-a", List.of());
        AgentDefinition specialist = definition("specialist", "model-b", List.of());
        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(planner));
        registry.register(new Agent(specialist));

        ILlmClient scripted = new ILlmClient()
        {
            int callCount = 0;

            @Override
            public LlmResponse chat(LlmRequest request)
            {
                callCount++;
                if (callCount == 1)
                {
                    return new LlmResponse("handoff", List.of(), "specialist", new TokenUsage(1, 1));
                }
                return new LlmResponse("specialist answer", List.of(), null, new TokenUsage(2, 2));
            }
        };

        AgentRunResult result = new AgentRunner(new DefaultStepEngine(scripted, new ToolRegistry(), registry,
            new DefaultContextBuilder(), new HookManager())).run(new Agent(planner), "Need specialist");

        assertEquals("specialist", result.getFinalAgentId());
        assertEquals("specialist answer", result.getOutput());
    }

    @Test
    void loadsDeclarativeDefinitionFromJson()
    {
        String json = """
            {
              "id":"agent-1",
              "name":"Declarative Agent",
              "systemPrompt":"You are helpful",
              "model":"gpt-4o-mini",
              "toolNames":["calculator"],
              "maxSteps":5
            }
            """;

        AgentDefinition definition = AgentDefinitionLoader.fromJson(json);
        assertEquals("agent-1", definition.getId());
        assertEquals(5, definition.getMaxSteps());
        assertTrue(definition.getToolNames().contains("calculator"));
    }

    @Test
    void invokesAgentHooksAndStopsAtMaxSteps()
    {
        AgentDefinition definition = definition("loop", "model", List.of());
        definition.setMaxSteps(2);

        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(definition));

        ILlmClient neverFinal = request -> new LlmResponse("", List.of(new ToolCall("noop", Map.of())), null, new TokenUsage(1, 1));
        ToolRegistry tools = new ToolRegistry();
        tools.register(new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition("noop", "", List.of());
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                return "ok";
            }
        });

        HookManager hooks = new HookManager();
        AtomicInteger stepCounter = new AtomicInteger();
        hooks.registerAgentHook(new IAgentHook()
        {
            @Override
            public void onStep(ExecutionContext context)
            {
                stepCounter.incrementAndGet();
            }
        });

        AgentRunResult result = new AgentRunner(new DefaultStepEngine(neverFinal, tools, registry,
            new DefaultContextBuilder(), hooks)).run(new Agent(definition), "loop");

        assertEquals("Stopped: max steps reached", result.getOutput());
        assertEquals(2, stepCounter.get());
    }

    @Test
    void streamsModelDeltasThroughStepEngineAndHooks()
    {
        AgentDefinition definition = definition("stream", "stream-model", List.of());
        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(definition));

        ILlmClient llmClient = new ILlmClient()
        {
            @Override
            public LlmResponse chat(LlmRequest request)
            {
                return new LlmResponse("streamed-response", List.of(), null, new TokenUsage(1, 1));
            }

            @Override
            public LlmResponse chatStream(LlmRequest request, LlmStreamListener listener)
            {
                listener.onDelta("streamed-");
                listener.onDelta("response");
                return new LlmResponse("streamed-response", List.of(), null, new TokenUsage(1, 1));
            }
        };

        HookManager hooks = new HookManager();
        AtomicReference<String> streamed = new AtomicReference<>("");
        hooks.registerAgentHook(new IAgentHook()
        {
            @Override
            public void onModelResponseDelta(ExecutionContext context, String delta)
            {
                streamed.updateAndGet(prev -> prev + delta);
            }
        });

        AgentRunner runner = new AgentRunner(new DefaultStepEngine(llmClient, new ToolRegistry(), registry,
            new DefaultContextBuilder(), hooks));

        AgentRunResult result = runner.runStreamed(new Agent(definition), "stream this");

        assertEquals("streamed-response", result.getOutput());
        assertEquals("streamed-response", streamed.get());
    }

    @Test
    void streamedAgentRunnerFallsBackToSyncClientWhenStreamingNotImplemented()
    {
        AgentDefinition definition = definition("sync-fallback", "model", List.of());
        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(definition));

        ILlmClient syncOnly = request -> new LlmResponse("sync-only", List.of(), null, new TokenUsage(1, 1));

        HookManager hooks = new HookManager();
        AtomicReference<String> streamed = new AtomicReference<>("");
        hooks.registerAgentHook(new IAgentHook()
        {
            @Override
            public void onModelResponseDelta(ExecutionContext context, String delta)
            {
                streamed.updateAndGet(prev -> prev + delta);
            }
        });

        AgentRunner runner = new AgentRunner(new DefaultStepEngine(syncOnly, new ToolRegistry(), registry,
            new DefaultContextBuilder(), hooks));

        AgentRunResult result = runner.runStreamed(new Agent(definition), "stream this");

        assertEquals("sync-only", result.getOutput());
        assertEquals("sync-only", streamed.get());
    }

    private static AgentDefinition definition(String id, String model, List<String> toolNames)
    {
        AgentDefinition definition = new AgentDefinition();
        definition.setId(id);
        definition.setName(id);
        definition.setSystemPrompt("System prompt for " + id);
        definition.setModel(model);
        definition.setToolNames(toolNames);
        return definition;
    }
}
