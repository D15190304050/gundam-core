package stark.dataworks.coderaider.gundam.core.examples;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.event.RunEventType;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.mcp.McpManager;
import stark.dataworks.coderaider.gundam.core.mcp.McpServerConfiguration;
import stark.dataworks.coderaider.gundam.core.mcp.McpToolDescriptor;
import stark.dataworks.coderaider.gundam.core.mcp.StdioMcpServerClient;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.streaming.IRunEventListener;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.builtin.mcp.HostedMcpTool;

/**
 * 3) How to create an agent with a set of MCPs, and then run it with streaming output.
 * 
 * Usage: java Example03AgentWithMcp [model] [apiKey] [query] [mcpServerCommand]
 * - model: ModelScope model name (default: Qwen/Qwen3-4B)
 * - apiKey: Your ModelScope API key (required, or set MODEL_SCOPE_API_KEY env var)
 * - query: Search query (default: "Find onboarding policy")
 * - mcpServerCommand: MCP server command (default: "python src/main/resources/mcp/simple_mcp_server_stdio.py")
 * 
 * Prerequisites:
 * 1. Install mcp package: pip install mcp[cli]
 * 2. Run this example - the MCP server will be started and terminated automatically.
 */
public class Example03AgentWithMcpTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        String query = "Find onboarding policy";
        String mcpServerCommand = McpTestSupport.pythonExecutable() + " src/main/resources/mcp/simple_mcp_server_stdio.py";

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.err.println("Set MODEL_SCOPE_API_KEY environment variable or pass as second argument.");
            System.exit(1);
        }

        StdioMcpServerClient mcpClient = new StdioMcpServerClient();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[MCP] Shutting down MCP server...");
            mcpClient.disconnect(new McpServerConfiguration("kb-mcp", mcpServerCommand, Map.of()));
        }));

        McpManager mcpManager = new McpManager(mcpClient);

        McpServerConfiguration mcpServer = new McpServerConfiguration(
            "kb-mcp",
            mcpServerCommand,
            Map.of());
        mcpManager.registerServer(mcpServer);

        List<McpToolDescriptor> tools = mcpClient.listTools(mcpServer);
        System.out.println("Available MCP tools: " + tools.stream().map(McpToolDescriptor::getName).toList());

        AgentDefinition agentDef = new AgentDefinition();
        agentDef.setId("mcp-agent");
        agentDef.setName("MCP Agent");
        agentDef.setModel(model);
        agentDef.setSystemPrompt("Use kb_search to answer questions from internal knowledge. Be concise and helpful.");
        agentDef.setToolNames(List.of("kb_search"));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(agentDef);

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(new HostedMcpTool("kb-mcp", "kb_search", mcpManager));

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(createConsoleStreamingPublisher())
            .build();

        System.out.print("Streaming output: ");
        ContextResult result = runner.chatClient("mcp-agent").prompt().user(query).runConfiguration(RunConfiguration.defaults()).runHooks(ExampleSupport.noopHooks()).call().contextResult();
        System.out.println();
        System.out.println("Final output: " + result.getFinalOutput());
        
        mcpClient.disconnect(mcpServer);
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        return ExampleStreamingPublishers.textWithToolLifecycle("MCP ");
    }
}
