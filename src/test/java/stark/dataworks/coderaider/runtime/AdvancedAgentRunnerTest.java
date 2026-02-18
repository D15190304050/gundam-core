package stark.dataworks.coderaider.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.agent.Agent;
import stark.dataworks.coderaider.agent.AgentDefinition;
import stark.dataworks.coderaider.agent.AgentRegistry;
import stark.dataworks.coderaider.approval.AllowAllToolApprovalPolicy;
import stark.dataworks.coderaider.context.DefaultContextBuilder;
import stark.dataworks.coderaider.event.RunEventType;
import stark.dataworks.coderaider.guardrail.GuardrailDecision;
import stark.dataworks.coderaider.guardrail.GuardrailEngine;
import stark.dataworks.coderaider.handoff.HandoffRouter;
import stark.dataworks.coderaider.hook.HookManager;
import stark.dataworks.coderaider.llmspi.LlmResponse;
import stark.dataworks.coderaider.metrics.TokenUsage;
import stark.dataworks.coderaider.model.ToolCall;
import stark.dataworks.coderaider.output.OutputSchema;
import stark.dataworks.coderaider.output.OutputSchemaRegistry;
import stark.dataworks.coderaider.output.OutputValidator;
import stark.dataworks.coderaider.result.RunItemType;
import stark.dataworks.coderaider.result.RunResult;
import stark.dataworks.coderaider.runner.AdvancedAgentRunner;
import stark.dataworks.coderaider.runner.RunConfig;
import stark.dataworks.coderaider.runner.RunHooks;
import stark.dataworks.coderaider.session.InMemorySessionStore;
import stark.dataworks.coderaider.streaming.RunEventPublisher;
import stark.dataworks.coderaider.tool.ITool;
import stark.dataworks.coderaider.tool.ToolDefinition;
import stark.dataworks.coderaider.tool.ToolRegistry;
import stark.dataworks.coderaider.tracing.NoopTraceProvider;

class AdvancedAgentRunnerTest {

    @Test
    void blocksByInputGuardrail() {
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

        RunResult result = runner.run(new Agent(def), "hello", RunConfig.defaults(), new RunHooks() {});
        assertTrue(result.getFinalOutput().contains("Blocked by input guardrail"));
        assertTrue(result.getEvents().stream().anyMatch(e -> e.getType() == RunEventType.GUARDRAIL_BLOCKED));
    }

    @Test
    void executesToolAndStoresSession() {
        AgentDefinition def = baseDef("agent");
        def.setToolNames(List.of("echo"));
        def.setMaxSteps(4);

        AgentRegistry agents = new AgentRegistry();
        agents.register(new Agent(def));

        ToolRegistry tools = new ToolRegistry();
        tools.register(new ITool() {
            @Override
            public ToolDefinition definition() {
                return new ToolDefinition("echo", "", List.of());
            }

            @Override
            public String execute(Map<String, Object> input) {
                return "ok";
            }
        });

        InMemorySessionStore sessions = new InMemorySessionStore();
        AdvancedAgentRunner runner = new AdvancedAgentRunner(
            req -> {
                if (req.getMessages().stream().noneMatch(m -> m.getContent().contains("echo: ok"))) {
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

        RunResult result = runner.run(new Agent(def), "start", new RunConfig(8, "s1", 0.1, 128, "auto", "text", Map.of()), new RunHooks() {});
        assertEquals("done", result.getFinalOutput());
        assertTrue(result.getItems().stream().anyMatch(i -> i.getType() == RunItemType.TOOL_RESULT));
        assertTrue(sessions.load("s1").isPresent());
    }

    @Test
    void validatesStructuredOutputSchema() {
        AgentDefinition def = baseDef("structured");
        def.setOutputSchemaName("summary");

        AgentRegistry agents = new AgentRegistry();
        agents.register(new Agent(def));

        OutputSchemaRegistry registry = new OutputSchemaRegistry();
        registry.register(new OutputSchema() {
            @Override
            public String name() {
                return "summary";
            }

            @Override
            public Map<String, String> requiredFields() {
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

        RunResult result = runner.run(new Agent(def), "go", RunConfig.defaults(), new RunHooks() {});
        assertEquals("ok", result.getFinalOutput());
    }

    private AgentDefinition baseDef(String id) {
        AgentDefinition d = new AgentDefinition();
        d.setId(id);
        d.setName(id);
        d.setModel("mock-model");
        d.setSystemPrompt("you are " + id);
        return d;
    }
}
