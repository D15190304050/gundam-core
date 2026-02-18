package stark.dataworks.coderaider.gundam.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.agent.Agent;
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
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmStreamListener;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
import stark.dataworks.coderaider.gundam.core.output.OutputSchema;
import stark.dataworks.coderaider.gundam.core.output.OutputSchemaRegistry;
import stark.dataworks.coderaider.gundam.core.output.OutputValidator;
import stark.dataworks.coderaider.gundam.core.result.RunItemType;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.AdvancedAgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfig;
import stark.dataworks.coderaider.gundam.core.runner.RunHooks;
import stark.dataworks.coderaider.gundam.core.session.InMemorySessionStore;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tracing.NoopTraceProvider;

class AdvancedAgentRunnerTest
{

    @Test
    void blocksByInputGuardrail()
    {
        GuardrailEngine guardrails = new GuardrailEngine();
        guardrails.registerInput((ctx, input) -> GuardrailDecision.deny("forbidden"));

        AgentDefinition def = baseDef("a1");
        AgentRegistry agents = new AgentRegistry();
        agents.register(new Agent(def));

        AdvancedAgentRunner runner = new AdvancedAgentRunner(
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

        RunResult result = runner.run(new Agent(def), "hello", RunConfig.defaults(), new RunHooks()
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
        agents.register(new Agent(def));

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
        AdvancedAgentRunner runner = new AdvancedAgentRunner(
            req ->
            {
                if (req.getMessages().stream().noneMatch(m -> m.getContent().contains("echo: ok")))
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

        RunResult result = runner.run(new Agent(def), "start", new RunConfig(8, "s1", 0.1, 128, "auto", "text", Map.of()), new RunHooks()
        {
        });
        assertEquals("done", result.getFinalOutput());
        assertTrue(result.getItems().stream().anyMatch(i -> i.getType() == RunItemType.TOOL_RESULT));
        assertTrue(sessions.load("s1").isPresent());
    }

    @Test
    void validatesStructuredOutputSchema()
    {
        AgentDefinition def = baseDef("structured");
        def.setOutputSchemaName("summary");

        AgentRegistry agents = new AgentRegistry();
        agents.register(new Agent(def));

        OutputSchemaRegistry registry = new OutputSchemaRegistry();
        registry.register(new OutputSchema()
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

        AdvancedAgentRunner runner = new AdvancedAgentRunner(
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

        RunResult result = runner.run(new Agent(def), "go", RunConfig.defaults(), new RunHooks()
        {
        });
        assertEquals("ok", result.getFinalOutput());
    }

    @Test
    void streamsModelDeltasDuringRun()
    {
        AgentDefinition def = baseDef("streaming");

        AgentRegistry agents = new AgentRegistry();
        agents.register(new Agent(def));

        AdvancedAgentRunner runner = new AdvancedAgentRunner(
            new ILlmClient()
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

        RunResult result = runner.runStreamed(new Agent(def), "go", RunConfig.defaults(), new RunHooks()
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
        agents.register(new Agent(def));

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

        AdvancedAgentRunner runner = new AdvancedAgentRunner(
            new ILlmClient()
            {
                int callCount = 0;

                @Override
                public LlmResponse chat(LlmRequest request)
                {
                    return new LlmResponse("", List.of(), null, new TokenUsage(0, 0));
                }

                @Override
                public LlmResponse chatStream(LlmRequest request, LlmStreamListener listener)
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

        RunResult result = runner.runStreamed(new Agent(def), "go", RunConfig.defaults(), new RunHooks()
        {
        });

        assertEquals("done", result.getFinalOutput());
        assertEquals(8, result.getUsage().getTotalTokens());
        assertTrue(result.getItems().stream().anyMatch(i -> i.getType() == RunItemType.TOOL_RESULT));
    }

    @Test
    void streamedRunFallsBackToSyncClientWhenStreamingNotImplemented()
    {
        AgentDefinition def = baseDef("fallback");

        AgentRegistry agents = new AgentRegistry();
        agents.register(new Agent(def));

        AdvancedAgentRunner runner = new AdvancedAgentRunner(
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

        RunResult result = runner.runStreamed(new Agent(def), "go", RunConfig.defaults(), new RunHooks()
        {
        });

        assertEquals("sync-only", result.getFinalOutput());
        List<String> deltas = result.getEvents().stream()
            .filter(e -> e.getType() == RunEventType.MODEL_RESPONSE_DELTA)
            .map(e -> (String) e.getAttributes().get("delta"))
            .toList();
        assertEquals(List.of("sync-only"), deltas);
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
