package stark.dataworks.coderaider.gundam.core.examples;

import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.mcp.InMemoryMcpServerClient;
import stark.dataworks.coderaider.gundam.core.mcp.McpManager;
import stark.dataworks.coderaider.gundam.core.mcp.McpServerConfiguration;
import stark.dataworks.coderaider.gundam.core.mcp.McpToolDescriptor;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.model.Role;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.session.InMemorySessionStore;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.builtin.mcp.HostedMcpTool;

/**
 * 4) How to run multi-round conversation by a single agent with tools & MCPs.
 */
public class Example04MultiRoundSingleAgentWithToolsAndMcp
{
    public static void main(String[] args)
    {
        String sessionId = args.length > 0 ? args[0] : "demo-session-001";

        AgentDefinition agentDef = new AgentDefinition();
        agentDef.setId("hybrid-agent");
        agentDef.setName("Hybrid Agent");
        agentDef.setModel("gpt-4.1-mini");
        agentDef.setSystemPrompt("Use tools and mcp tools for planning answers.");
        agentDef.setToolNames(List.of("calc_tax", "kb_search"));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(agentDef));

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition("calc_tax", "calculate tax", List.of());
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                return "tax=12.5";
            }
        });

        InMemoryMcpServerClient mcpClient = new InMemoryMcpServerClient();
        McpManager mcpManager = new McpManager(mcpClient);
        mcpManager.registerServer(new McpServerConfiguration("policy-mcp", "http://localhost:9100/mcp", Map.of()));
        mcpClient.registerTools("policy-mcp", List.of(new McpToolDescriptor("kb_search", "policy search", Map.of())));
        mcpClient.registerHandler("policy-mcp", "kb_search", in -> "policy says reimbursement cap is 500");
        toolRegistry.register(new HostedMcpTool("policy-mcp", "kb_search", mcpManager));

        InMemorySessionStore sessionStore = new InMemorySessionStore();
        AgentRunner runner = ExampleSupport.runner(new MultiRoundClient(), toolRegistry, agentRegistry, sessionStore);

        RunConfiguration config = new RunConfiguration(8, sessionId, 0.2, 512, "auto", "text", Map.of());

        RunResult round1 = runner.run(agentRegistry.get("hybrid-agent").orElseThrow(), "Please estimate tax for amount 100.", config, ExampleSupport.noopHooks());
        RunResult round2 = runner.run(agentRegistry.get("hybrid-agent").orElseThrow(), "What policy constraints should I know?", config, ExampleSupport.noopHooks());

        System.out.println("Round1=" + round1.getFinalOutput());
        System.out.println("Round2=" + round2.getFinalOutput());

        List<Message> sessionMessages = sessionStore.load(sessionId).orElseThrow().getMessages();
        System.out.println("PersistedMessageCount=" + sessionMessages.size());
    }

    private static class MultiRoundClient implements ILlmClient
    {
        @Override
        public LlmResponse chat(LlmRequest request)
        {
            String latestUser = request.getMessages().stream()
                .filter(message -> message.getRole() == Role.USER)
                .reduce((a, b) -> b)
                .map(Message::getContent)
                .orElse("");

            boolean hasToolOutput = request.getMessages().stream().anyMatch(message -> message.getRole() == Role.TOOL);
            if (!hasToolOutput && latestUser.toLowerCase().contains("tax"))
            {
                return new LlmResponse("", List.of(new ToolCall("calc_tax", Map.of("amount", 100))), null, new TokenUsage(10, 5));
            }
            if (!hasToolOutput && latestUser.toLowerCase().contains("policy"))
            {
                return new LlmResponse("", List.of(new ToolCall("kb_search", Map.of("query", "expense policy"))), null, new TokenUsage(10, 5));
            }
            if (latestUser.toLowerCase().contains("policy"))
            {
                return new LlmResponse("Policy cap is 500 and approvals are required.", List.of(), null, new TokenUsage(8, 14));
            }
            return new LlmResponse("Estimated tax is 12.5.", List.of(), null, new TokenUsage(8, 12));
        }
    }
}
