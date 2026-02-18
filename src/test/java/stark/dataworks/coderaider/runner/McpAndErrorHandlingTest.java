package stark.dataworks.coderaider.runner;

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
import stark.dataworks.coderaider.guardrail.GuardrailEngine;
import stark.dataworks.coderaider.handoff.HandoffRouter;
import stark.dataworks.coderaider.hook.HookManager;
import stark.dataworks.coderaider.llmspi.LlmResponse;
import stark.dataworks.coderaider.mcp.InMemoryMcpServerClient;
import stark.dataworks.coderaider.mcp.McpManager;
import stark.dataworks.coderaider.mcp.McpServerConfig;
import stark.dataworks.coderaider.mcp.McpToolDescriptor;
import stark.dataworks.coderaider.mcp.McpResource;
import stark.dataworks.coderaider.mcp.McpResourceTemplate;
import stark.dataworks.coderaider.metrics.TokenUsage;
import stark.dataworks.coderaider.output.OutputSchemaRegistry;
import stark.dataworks.coderaider.output.OutputValidator;
import stark.dataworks.coderaider.result.RunResult;
import stark.dataworks.coderaider.runerror.RunErrorHandlers;
import stark.dataworks.coderaider.runerror.RunErrorKind;
import stark.dataworks.coderaider.runerror.RunErrorHandlerResult;
import stark.dataworks.coderaider.session.InMemorySessionStore;
import stark.dataworks.coderaider.streaming.RunEventPublisher;
import stark.dataworks.coderaider.tool.ToolRegistry;
import stark.dataworks.coderaider.tool.builtin.WebSearchTool;
import stark.dataworks.coderaider.tool.ToolDefinition;
import stark.dataworks.coderaider.tracing.NoopTraceProvider;

class McpAndErrorHandlingTest {

    @Test
    void mcpManagerCanExposeRemoteToolsAsLocal() {
        InMemoryMcpServerClient client = new InMemoryMcpServerClient();
        client.registerTools("s1", List.of(new McpToolDescriptor("remote_search", "remote", Map.of())));
        client.registerHandler("s1", "remote_search", args -> "remote:" + args.getOrDefault("q", ""));

        client.registerResources("s1", List.of(new McpResource("res://a", "text/plain", "A")));
        client.registerResourceTemplates("s1", List.of(new McpResourceTemplate("tpl", "res://{id}", "template")));

        McpManager manager = new McpManager(client);
        manager.registerServer(new McpServerConfig("s1", "http://localhost", Map.of()));

        String output = manager.resolveToolsAsLocalTools("s1").get(0).execute(Map.of("q", "java"));
        assertEquals("remote:java", output);
        assertEquals(1, manager.listResources("s1").size());
        assertEquals("A", manager.readResource("s1", "res://a").content());
        assertEquals(1, manager.listResourceTemplates("s1").size());
    }

    @Test
    void runErrorHandlerCanHandleModelFailures() {
        AgentDefinition def = new AgentDefinition();
        def.setId("a");
        def.setName("a");
        def.setSystemPrompt("sys");
        def.setModel("m");

        AgentRegistry agents = new AgentRegistry();
        agents.register(new Agent(def));

        RunErrorHandlers handlers = new RunErrorHandlers();
        handlers.register(RunErrorKind.MODEL_INVOCATION, data -> RunErrorHandlerResult.handled("fallback output"));

        AdvancedAgentRunner runner = new AdvancedAgentRunner(
            req -> { throw new RuntimeException("model down"); },
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

        RunConfig cfg = new RunConfig(2, null, 0.2, 128, "auto", "text", Map.of(),
            new stark.dataworks.coderaider.policy.RetryPolicy(1, 0), handlers);

        RunResult result = runner.run(new Agent(def), "hello", cfg, event -> {});
        assertEquals("fallback output", result.getFinalOutput());
        assertTrue(result.getEvents().stream().anyMatch(e -> e.getType().name().equals("RUN_FAILED")));
    }

    @Test
    void builtinToolEcosystemHasWebSearchTool() {
        WebSearchTool tool = new WebSearchTool(new ToolDefinition("web_search", "", List.of()));
        assertTrue(tool.execute(Map.of("query", "gundam")).contains("gundam"));
    }
}
