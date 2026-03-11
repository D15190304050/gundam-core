package stark.dataworks.coderaider.genericagent.core.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.genericagent.core.agent.AgentDefinition;
import stark.dataworks.coderaider.genericagent.core.agent.AgentRegistry;
import stark.dataworks.coderaider.genericagent.core.approval.AllowAllToolApprovalPolicy;
import stark.dataworks.coderaider.genericagent.core.context.DefaultContextBuilder;
import stark.dataworks.coderaider.genericagent.core.guardrail.GuardrailEngine;
import stark.dataworks.coderaider.genericagent.core.handoff.HandoffRouter;
import stark.dataworks.coderaider.genericagent.core.policy.RetryPolicy;
import stark.dataworks.coderaider.genericagent.core.hooks.HookManager;
import stark.dataworks.coderaider.genericagent.core.mcp.InMemoryMcpServerClient;
import stark.dataworks.coderaider.genericagent.core.mcp.McpManager;
import stark.dataworks.coderaider.genericagent.core.mcp.McpServerConfiguration;
import stark.dataworks.coderaider.genericagent.core.mcp.McpToolDescriptor;
import stark.dataworks.coderaider.genericagent.core.mcp.McpResource;
import stark.dataworks.coderaider.genericagent.core.mcp.McpResourceTemplate;
import stark.dataworks.coderaider.genericagent.core.output.OutputSchemaRegistry;
import stark.dataworks.coderaider.genericagent.core.output.OutputValidator;
import stark.dataworks.coderaider.genericagent.core.context.ContextResult;
import stark.dataworks.coderaider.genericagent.core.runerror.RunErrorHandlers;
import stark.dataworks.coderaider.genericagent.core.runerror.RunErrorKind;
import stark.dataworks.coderaider.genericagent.core.runerror.RunErrorHandlerResult;
import stark.dataworks.coderaider.genericagent.core.session.InMemorySessionStore;
import stark.dataworks.coderaider.genericagent.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.genericagent.core.tool.ToolRegistry;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.WebSearchTool;
import stark.dataworks.coderaider.genericagent.core.tool.ToolDefinition;
import stark.dataworks.coderaider.genericagent.core.tracing.NoopTraceProvider;

class McpAndErrorHandlingTest
{

    @Test
    void mcpManagerCanExposeRemoteToolsAsLocal()
    {
        InMemoryMcpServerClient client = new InMemoryMcpServerClient();
        client.registerTools("s1", List.of(new McpToolDescriptor("remote_search", "remote", Map.of())));
        client.registerHandler("s1", "remote_search", args -> "remote:" + args.getOrDefault("q", ""));

        client.registerResources("s1", List.of(new McpResource("res://a", "text/plain", "A")));
        client.registerResourceTemplates("s1", List.of(new McpResourceTemplate("tpl", "res://{id}", "template")));

        McpManager manager = new McpManager(client);
        manager.registerServer(new McpServerConfiguration("s1", "http://localhost", Map.of()));

        String output = manager.resolveToolsAsLocalTools("s1").get(0).execute(Map.of("q", "java"));
        assertEquals("remote:java", output);
        assertEquals(1, manager.listResources("s1").size());
        assertEquals("A", manager.readResource("s1", "res://a").content());
        assertEquals(1, manager.listResourceTemplates("s1").size());
    }

    @Test
    void runErrorHandlerCanHandleModelFailures()
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("a");
        def.setName("a");
        def.setSystemPrompt("sys");
        def.setModel("m");

        AgentRegistry agents = new AgentRegistry();
        agents.register(def);

        RunErrorHandlers handlers = new RunErrorHandlers();
        handlers.register(RunErrorKind.MODEL_INVOCATION, data -> RunErrorHandlerResult.handled("fallback output"));

        AgentRunner runner = new AgentRunner(
            req ->
            {
                throw new RuntimeException("model down");
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

        RunConfiguration cfg = new RunConfiguration(2, null, 0.2, 128, "auto", "text", Map.of(),
            new RetryPolicy(1, 0), handlers);

        ContextResult result = runner.run(def, "hello", cfg, new IRunHooks()
        {
        });
        assertEquals("fallback output", result.getFinalOutput());
        assertTrue(result.getEvents().stream().anyMatch(e -> e.getType().name().equals("RUN_FAILED")));
    }

    @Test
    void builtinToolEcosystemHasWebSearchTool()
    {
        WebSearchTool tool = new WebSearchTool(new ToolDefinition("web_search", "", List.of()), "");
        // TODO: set BING_SEARCH_V7_SUBSCRIPTION_KEY locally to validate live Bing responses.
        assertTrue(tool.execute(Map.of("query", "gundam")).startsWith("WebSearch("));
    }
}
