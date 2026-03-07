package stark.dataworks.coderaider.gundam.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.approval.AllowAllToolApprovalPolicy;
import stark.dataworks.coderaider.gundam.core.context.DefaultContextBuilder;
import stark.dataworks.coderaider.gundam.core.event.RunEventType;
import stark.dataworks.coderaider.gundam.core.guardrail.GuardrailDecision;
import stark.dataworks.coderaider.gundam.core.guardrail.GuardrailEngine;
import stark.dataworks.coderaider.gundam.core.handoff.HandoffRouter;
import stark.dataworks.coderaider.gundam.core.hook.HookManager;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmClientRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmStreamListener;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.memory.InMemoryAgentMemory;
import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.model.Role;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
import stark.dataworks.coderaider.gundam.core.output.IOutputSchema;
import stark.dataworks.coderaider.gundam.core.output.OutputSchemaRegistry;
import stark.dataworks.coderaider.gundam.core.output.OutputValidator;
import stark.dataworks.coderaider.gundam.core.context.ContextItemType;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.runner.IRunHooks;
import stark.dataworks.coderaider.gundam.core.session.InMemorySessionStore;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tracing.NoopTraceProvider;

class AgentRunnerTest
{

    @Test
    void blocksByInputGuardrail()
    {
        GuardrailEngine guardrails = new GuardrailEngine();
        guardrails.registerInput((ctx, input) -> GuardrailDecision.deny("forbidden"));

        AgentDefinition def = baseDef("a1");
        AgentRegistry agents = new AgentRegistry();
        agents.register(def);

        AgentRunner runner = new AgentRunner(
            req -> new LlmResponse("should not happen", List.of(), null, new TokenUsage(0, 0)),
            new ToolRegistry(),
            agents,
            new DefaultContextBuilder(),
            new HookManager(),
            guardrails,
            new HandoffRouter(),
            new InMemorySessionStore(),
            new NoopTraceProvider(),
            new AllowAllToolApprovalPolicy(),
            new OutputSchemaRegistry(),
            new OutputValidator(),
            new RunEventPublisher());

        ContextResult result = runner.run(def, "hello", RunConfiguration.defaults(), new IRunHooks()
        {
        });
        assertTrue(result.getFinalOutput().contains("Blocked by input guardrail"));
        assertTrue(result.getEvents().stream().anyMatch(e -> e.getType() == RunEventType.GUARDRAIL_BLOCKED));
    }

    @Test
    void executesToolAndStoresSession()
    {
        AgentDefinition def = baseDef("agent");
        def.setToolNames(List.of("echo"));
        def.setMaxSteps(4);

        AgentRegistry agents = new AgentRegistry();
        agents.register(def);

        ToolRegistry tools = new ToolRegistry();
        tools.register(new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition("echo", "", List.of());
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                return "ok";
            }
        });

        InMemorySessionStore sessions = new InMemorySessionStore();
        AgentRunner runner = new AgentRunner(
            req ->
            {
                if (req.getMessages().stream().noneMatch(m -> "ok".equals(m.getContent())))
                {
                    return new LlmResponse("", List.of(new ToolCall("echo", Map.of())), null, new TokenUsage(1, 1));
                }
                return new LlmResponse("done", List.of(), null, new TokenUsage(2, 2));
            },
            tools,
            agents,
            new DefaultContextBuilder(),
            new HookManager(),
            new GuardrailEngine(),
            new HandoffRouter(),
            sessions,
            new NoopTraceProvider(),
            new AllowAllToolApprovalPolicy(),
            new OutputSchemaRegistry(),
            new OutputValidator(),
            new RunEventPublisher());

        ContextResult result = runner.run(def, "start", new RunConfiguration(8, "s1", 0.1, 128, "auto", "text", Map.of()), new IRunHooks()
        {
        });
        assertEquals("done", result.getFinalOutput());
        assertTrue(result.getItems().stream().anyMatch(i -> i.getType() == ContextItemType.TOOL_RESULT));
        assertTrue(sessions.load("s1").isPresent());
    }

    @Test
    void usesCallerProvidedMemoryWhenConfigured()
    {
        AgentDefinition def = baseDef("memory-agent");
        AgentRegistry agents = new AgentRegistry();
        agents.register(def);

        InMemorySessionStore sessions = new InMemorySessionStore();
        sessions.save(new stark.dataworks.coderaider.gundam.core.session.Session("memory-session", List.of(new Message(Role.USER, "session-history"))));

        InMemoryAgentMemory customMemory = new InMemoryAgentMemory();
        AtomicBoolean sawSessionHistory = new AtomicBoolean(false);

        AgentRunner runner = new AgentRunner(
            req ->
            {
                sawSessionHistory.set(req.getMessages().stream().anyMatch(m -> "session-history".equals(m.getContent())));
                return new LlmResponse("done", List.of(), null, new TokenUsage(1, 1));
            },
            new ToolRegistry(),
            agents,
            new DefaultContextBuilder(),
            new HookManager(),
            new GuardrailEngine(),
            new HandoffRouter(),
            sessions,
            new NoopTraceProvider(),
            new AllowAllToolApprovalPolicy(),
            new OutputSchemaRegistry(),
            new OutputValidator(),
            new RunEventPublisher());

        RunConfiguration configuration = new RunConfiguration(8, "memory-session", 0.1, 128, "auto", "text", Map.of(), null, null, customMemory);
        ContextResult result = runner.run(def, "start", configuration, new IRunHooks()
        {
        });

        assertEquals("done", result.getFinalOutput());
        assertTrue(sawSessionHistory.get());
    }

    @Test
    void validatesStructuredOutputSchema()
    {
        AgentDefinition def = baseDef("structured");
        def.setOutputSchemaName("summary");

        AgentRegistry agents = new AgentRegistry();
        agents.register(def);

        OutputSchemaRegistry registry = new OutputSchemaRegistry();
        registry.register(new IOutputSchema()
        {
            @Override
            public String name()
            {
                return "summary";
            }

            @Override
            public Map<String, String> requiredFields()
            {
                return Map.of("title", "string");
            }
        });

        AgentRunner runner = new AgentRunner(
            req -> new LlmResponse("ok", List.of(), null, new TokenUsage(1, 1), "stop", Map.of("title", "Hello")),
            new ToolRegistry(),
            agents,
            new DefaultContextBuilder(),
            new HookManager(),
            new GuardrailEngine(),
            new HandoffRouter(),
            new InMemorySessionStore(),
            new NoopTraceProvider(),
            new AllowAllToolApprovalPolicy(),
            registry,
            new OutputValidator(),
            new RunEventPublisher());

        ContextResult result = runner.run(def, "go", RunConfiguration.defaults(), new IRunHooks()
        {
        });
        assertEquals("ok", result.getFinalOutput());
    }

    @Test
    void streamsModelDeltasDuringRun()
    {
        AgentDefinition def = baseDef("streaming");

        AgentRegistry agents = new AgentRegistry();
        agents.register(def);

        AgentRunner runner = new AgentRunner(
            new ILlmClient()
            {
                @Override
                public LlmResponse chat(LlmRequest request)
                {
                    return new LlmResponse("streamed-response", List.of(), null, new TokenUsage(1, 1));
                }

                @Override
                public LlmResponse chatStream(LlmRequest request, ILlmStreamListener listener)
                {
                    listener.onDelta("streamed-");
                    listener.onDelta("response");
                    return new LlmResponse("streamed-response", List.of(), null, new TokenUsage(1, 1));
                }
            },
            new ToolRegistry(),
            agents,
            new DefaultContextBuilder(),
            new HookManager(),
            new GuardrailEngine(),
            new HandoffRouter(),
            new InMemorySessionStore(),
            new NoopTraceProvider(),
            new AllowAllToolApprovalPolicy(),
            new OutputSchemaRegistry(),
            new OutputValidator(),
            new RunEventPublisher());

        ContextResult result = runner.runStreamed(def, "go", RunConfiguration.defaults(), new IRunHooks()
        {
        });

        assertEquals("streamed-response", result.getFinalOutput());
        List<String> deltas = result.getEvents().stream()
            .filter(e -> e.getType() == RunEventType.MODEL_RESPONSE_DELTA)
            .map(e -> (String) e.getAttributes().get("delta"))
            .toList();
        assertFalse(deltas.isEmpty());
        assertEquals("streamed-response", String.join("", deltas));
    }


    @Test
    void streamedRunUsesToolCallsAndTokenUsageFromStreamingEvents()
    {
        AgentDefinition def = baseDef("stream-meta");
        def.setToolNames(List.of("echo"));

        AgentRegistry agents = new AgentRegistry();
        agents.register(def);

        ToolRegistry tools = new ToolRegistry();
        tools.register(new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition("echo", "", List.of());
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                return "ok";
            }
        });

        AgentRunner runner = new AgentRunner(
            new ILlmClient()
            {
                int callCount = 0;

                @Override
                public LlmResponse chat(LlmRequest request)
                {
                    return new LlmResponse("", List.of(), null, new TokenUsage(0, 0));
                }

                @Override
                public LlmResponse chatStream(LlmRequest request, ILlmStreamListener listener)
                {
                    callCount++;
                    if (callCount == 1)
                    {
                        listener.onToolCall(new ToolCall("echo", Map.of()));
                        listener.onTokenUsage(new TokenUsage(2, 3));
                        return new LlmResponse("", List.of(), null, new TokenUsage(0, 0));
                    }

                    listener.onDelta("done");
                    listener.onTokenUsage(new TokenUsage(1, 2));
                    return new LlmResponse("", List.of(), null, new TokenUsage(0, 0));
                }
            },
            tools,
            agents,
            new DefaultContextBuilder(),
            new HookManager(),
            new GuardrailEngine(),
            new HandoffRouter(),
            new InMemorySessionStore(),
            new NoopTraceProvider(),
            new AllowAllToolApprovalPolicy(),
            new OutputSchemaRegistry(),
            new OutputValidator(),
            new RunEventPublisher());

        ContextResult result = runner.runStreamed(def, "go", RunConfiguration.defaults(), new IRunHooks()
        {
        });

        assertEquals("done", result.getFinalOutput());
        assertEquals(8, result.getUsage().getTotalTokens());
        assertTrue(result.getItems().stream().anyMatch(i -> i.getType() == ContextItemType.TOOL_RESULT));
    }

    @Test
    void streamedRunFallsBackToSyncClientWhenStreamingNotImplemented()
    {
        AgentDefinition def = baseDef("fallback");

        AgentRegistry agents = new AgentRegistry();
        agents.register(def);

        AgentRunner runner = new AgentRunner(
            request -> new LlmResponse("sync-only", List.of(), null, new TokenUsage(1, 1)),
            new ToolRegistry(),
            agents,
            new DefaultContextBuilder(),
            new HookManager(),
            new GuardrailEngine(),
            new HandoffRouter(),
            new InMemorySessionStore(),
            new NoopTraceProvider(),
            new AllowAllToolApprovalPolicy(),
            new OutputSchemaRegistry(),
            new OutputValidator(),
            new RunEventPublisher());

        ContextResult result = runner.runStreamed(def, "go", RunConfiguration.defaults(), new IRunHooks()
        {
        });

        assertEquals("sync-only", result.getFinalOutput());
        List<String> deltas = result.getEvents().stream()
            .filter(e -> e.getType() == RunEventType.MODEL_RESPONSE_DELTA)
            .map(e -> (String) e.getAttributes().get("delta"))
            .toList();
        assertEquals(List.of("sync-only"), deltas);
    }


    @Test
    void streamedRunEmitsReasoningDeltaEvents()
    {
        AgentDefinition def = baseDef("reasoning");

        AgentRegistry agents = new AgentRegistry();
        agents.register(def);

        AgentRunner runner = new AgentRunner(
            new ILlmClient()
            {
                @Override
                public LlmResponse chat(LlmRequest request)
                {
                    return new LlmResponse("answer", List.of(), null, new TokenUsage(1, 1), "stop", "r-sync", Map.of(), List.of());
                }

                @Override
                public LlmResponse chatStream(LlmRequest request, ILlmStreamListener listener)
                {
                    listener.onReasoningDelta("step 1");
                    listener.onDelta("answer");
                    return new LlmResponse("answer", List.of(), null, new TokenUsage(1, 1), "stop", "step 1", Map.of(), List.of());
                }
            },
            new ToolRegistry(),
            agents,
            new DefaultContextBuilder(),
            new HookManager(),
            new GuardrailEngine(),
            new HandoffRouter(),
            new InMemorySessionStore(),
            new NoopTraceProvider(),
            new AllowAllToolApprovalPolicy(),
            new OutputSchemaRegistry(),
            new OutputValidator(),
            new RunEventPublisher());

        ContextResult result = runner.runStreamed(def, "go", RunConfiguration.defaults(), new IRunHooks()
        {
        });

        List<String> reasoningDeltas = result.getEvents().stream()
            .filter(e -> e.getType() == RunEventType.MODEL_REASONING_DELTA)
            .map(e -> (String) e.getAttributes().get("delta"))
            .toList();
        assertEquals(List.of("step 1"), reasoningDeltas);
    }

    @Test
    void mergesAgentReasoningAndSkillsIntoProviderOptions()
    {
        AgentDefinition def = baseDef("skills");
        def.setModelReasoning(Map.of("effort", "low"));
        def.setModelSkills(List.of(Map.of("type", "skill_reference", "skill_id", "skill-123")));

        AgentRegistry agents = new AgentRegistry();
        agents.register(def);

        AgentRunner runner = new AgentRunner(
            request ->
            {
                assertEquals("low", ((Map<?, ?>) request.getOptions().getProviderOptions().get("reasoning")).get("effort"));
                assertFalse(((List<?>) request.getOptions().getProviderOptions().get("skills")).isEmpty());
                return new LlmResponse("ok", List.of(), null, new TokenUsage(1, 1));
            },
            new ToolRegistry(),
            agents,
            new DefaultContextBuilder(),
            new HookManager(),
            new GuardrailEngine(),
            new HandoffRouter(),
            new InMemorySessionStore(),
            new NoopTraceProvider(),
            new AllowAllToolApprovalPolicy(),
            new OutputSchemaRegistry(),
            new OutputValidator(),
            new RunEventPublisher());

        ContextResult result = runner.run(def, "go", RunConfiguration.defaults(), new IRunHooks()
        {
        });

        assertEquals("ok", result.getFinalOutput());
    }

    @Test
    void supportsStructuredOutputTypeFromClass()
    {
        AgentDefinition def = baseDef("typed-schema");

        AgentRegistry agents = new AgentRegistry();
        agents.register(def);

        AgentRunner runner = AgentRunner.builder()
            .llmClient(request ->
            {
                assertEquals("json_schema", request.getOptions().getResponseFormat());
                assertNotNull(request.getOptions().getProviderOptions().get("responseFormatJsonSchema"));
                return new LlmResponse("{\"title\":\"hello\",\"score\":1}", List.of(), null, new TokenUsage(1, 1), "stop", Map.of("title", "hello", "score", 1));
            })
            .toolRegistry(new ToolRegistry())
            .agentRegistry(agents)
            .build();

        ContextResult result = runner.run(def, "go", RunConfiguration.defaults(), new IRunHooks()
        {
        }, ScoreSummary.class);

        assertEquals("{\"title\":\"hello\",\"score\":1}", result.getFinalOutput());
    }

    @Test
    void builderCreatesRunnerWithDefaults()
    {
        AgentDefinition def = baseDef("builder");
        AgentRegistry agents = new AgentRegistry();
        agents.register(def);

        AgentRunner runner = AgentRunner.builder()
            .llmClient(request -> new LlmResponse("ok", List.of(), null, new TokenUsage(1, 1)))
            .toolRegistry(new ToolRegistry())
            .agentRegistry(agents)
            .build();

        ContextResult result = runner.run(def, "hello", RunConfiguration.defaults(), new IRunHooks()
        {
        });

        assertEquals("ok", result.getFinalOutput());
    }


    @Test
    void resolvesLlmClientFromRegistryByModelPrefix()
    {
        AgentDefinition def = baseDef("multi-client");
        def.setModel("Qwen/Qwen3-4B");

        AgentRegistry agents = new AgentRegistry();
        agents.register(def);

        ILlmClient qwenClient = request -> new LlmResponse("qwen-ok", List.of(), null, new TokenUsage(1, 1));
        ILlmClient defaultClient = request -> new LlmResponse("default-ok", List.of(), null, new TokenUsage(1, 1));

        AgentRunner runner = AgentRunner.builder()
            .llmClientRegistry(new LlmClientRegistry(Map.of("Qwen", qwenClient, "default", defaultClient), "default"))
            .toolRegistry(new ToolRegistry())
            .agentRegistry(agents)
            .build();

        ContextResult result = runner.run(def, "hello", RunConfiguration.defaults(), new IRunHooks()
        {
        });

        assertEquals("qwen-ok", result.getFinalOutput());
    }


    @Test
    void executesMultipleToolCallsInParallelUsingVirtualThreads()
    {
        AgentDefinition def = baseDef("parallel-tools");
        def.setToolNames(List.of("slow_a", "slow_b"));
        def.setMaxSteps(4);

        AgentRegistry agents = new AgentRegistry();
        agents.register(def);

        AtomicBoolean usedVirtualThread = new AtomicBoolean(true);
        CountDownLatch started = new CountDownLatch(2);

        ToolRegistry tools = new ToolRegistry();
        tools.register(slowTool("slow_a", started, usedVirtualThread));
        tools.register(slowTool("slow_b", started, usedVirtualThread));

        AgentRunner runner = new AgentRunner(
            request ->
            {
                if (request.getMessages().stream().noneMatch(message -> message.getRole().name().equals("TOOL")))
                {
                    return new LlmResponse("", List.of(
                        new ToolCall("slow_a", Map.of("delay", 180)),
                        new ToolCall("slow_b", Map.of("delay", 180))), null, new TokenUsage(1, 1));
                }
                return new LlmResponse("done", List.of(), null, new TokenUsage(1, 1));
            },
            tools,
            agents,
            new DefaultContextBuilder(),
            new HookManager(),
            new GuardrailEngine(),
            new HandoffRouter(),
            new InMemorySessionStore(),
            new NoopTraceProvider(),
            new AllowAllToolApprovalPolicy(),
            new OutputSchemaRegistry(),
            new OutputValidator(),
            new RunEventPublisher());

        assertTimeoutPreemptively(Duration.ofMillis(350), () ->
        {
            ContextResult result = runner.run(def, "go", RunConfiguration.defaults(), new IRunHooks()
            {
            });
            assertEquals("done", result.getFinalOutput());
        });

        try
        {
            assertTrue(started.await(1, TimeUnit.SECONDS));
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        assertTrue(usedVirtualThread.get());
    }

    private static ITool slowTool(String name, CountDownLatch started, AtomicBoolean usedVirtualThread)
    {
        return new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition(name, "", List.of());
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                usedVirtualThread.compareAndSet(true, Thread.currentThread().isVirtual());
                started.countDown();
                try
                {
                    Thread.sleep(((Number) input.getOrDefault("delay", 180)).longValue());
                }
                catch (InterruptedException ex)
                {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
                return name + "-ok";
            }
        };
    }

    private static final class ScoreSummary
    {
        private String title;
        private int score;
    }

    private AgentDefinition baseDef(String id)
    {
        AgentDefinition d = new AgentDefinition();
        d.setId(id);
        d.setName(id);
        d.setModel("mock-model");
        d.setSystemPrompt("you are " + id);
        return d;
    }
}
