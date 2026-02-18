package stark.dataworks.coderaider.gundam.core.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.approval.AllowAllToolApprovalPolicy;
import stark.dataworks.coderaider.gundam.core.context.DefaultContextBuilder;
import stark.dataworks.coderaider.gundam.core.guardrail.GuardrailEngine;
import stark.dataworks.coderaider.gundam.core.handoff.HandoffRouter;
import stark.dataworks.coderaider.gundam.core.policy.RetryPolicy;
import stark.dataworks.coderaider.gundam.core.hook.HookManager;
import stark.dataworks.coderaider.gundam.core.mcp.InMemoryMcpServerClient;
import stark.dataworks.coderaider.gundam.core.mcp.McpManager;
import stark.dataworks.coderaider.gundam.core.mcp.McpServerConfig;
import stark.dataworks.coderaider.gundam.core.mcp.McpToolDescriptor;
import stark.dataworks.coderaider.gundam.core.mcp.McpResource;
import stark.dataworks.coderaider.gundam.core.mcp.McpResourceTemplate;
import stark.dataworks.coderaider.gundam.core.output.OutputSchemaRegistry;
import stark.dataworks.coderaider.gundam.core.output.OutputValidator;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runerror.RunErrorHandlers;
import stark.dataworks.coderaider.gundam.core.runerror.RunErrorKind;
import stark.dataworks.coderaider.gundam.core.runerror.RunErrorHandlerResult;
import stark.dataworks.coderaider.gundam.core.runner.AdvancedAgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfig;
import stark.dataworks.coderaider.gundam.core.session.InMemorySessionStore;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.builtin.WebSearchTool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tracing.NoopTraceProvider;

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
            new RetryPolicy(1, 0), handlers);

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
