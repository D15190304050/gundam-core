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
import stark.dataworks.coderaider.gundam.core.model.Role;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.builtin.mcp.HostedMcpTool;

/**
 * 3) How to create an agent with a set of MCPs, and then run it.
 */
public class Example03AgentWithMcp
{
    public static void main(String[] args)
    {
        InMemoryMcpServerClient mcpClient = new InMemoryMcpServerClient();
        McpManager mcpManager = new McpManager(mcpClient);

        McpServerConfiguration mcpServer = new McpServerConfiguration(
            "kb-mcp",
            "http://localhost:9000/mcp",
            Map.of("apiKey", "<your-mcp-api-key>", "baseUrl", "<your-mcp-base-url>"));
        mcpManager.registerServer(mcpServer);
        mcpClient.registerTools("kb-mcp", List.of(new McpToolDescriptor("kb_search", "Knowledge search", Map.of())));
        mcpClient.registerHandler("kb-mcp", "kb_search", in -> "MCP(search): top hit for " + in.getOrDefault("query", ""));

        AgentDefinition agentDef = new AgentDefinition();
        agentDef.setId("mcp-agent");
        agentDef.setName("MCP Agent");
        agentDef.setModel("gpt-4.1-mini");
        agentDef.setSystemPrompt("Use kb_search to answer questions from internal knowledge.");
        agentDef.setToolNames(List.of("kb_search"));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(agentDef));

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(new HostedMcpTool("kb-mcp", "kb_search", mcpManager));

        AgentRunner runner = ExampleSupport.runner(new McpCallingClient(), toolRegistry, agentRegistry, null);
        RunResult result = runner.run(agentRegistry.get("mcp-agent").orElseThrow(), "Find onboarding policy", RunConfiguration.defaults(), ExampleSupport.noopHooks());

        System.out.println("Output=" + result.getFinalOutput());
    }

    private static class McpCallingClient implements ILlmClient
    {
        @Override
        public LlmResponse chat(LlmRequest request)
        {
            boolean hasMcpResult = request.getMessages().stream().anyMatch(m -> m.getRole() == Role.TOOL);
            if (!hasMcpResult)
            {
                return new LlmResponse("", List.of(new ToolCall("kb_search", Map.of("query", "onboarding policy"))), null, new TokenUsage(11, 6));
            }
            return new LlmResponse("According to MCP KB, onboarding requires manager approval.", List.of(), null, new TokenUsage(8, 16));
        }
    }
}
