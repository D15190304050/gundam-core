package stark.dataworks.coderaider.gundam.core.examples;

import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.event.RunEventType;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.mcp.McpManager;
import stark.dataworks.coderaider.gundam.core.mcp.McpServerConfiguration;
import stark.dataworks.coderaider.gundam.core.mcp.McpToolDescriptor;
import stark.dataworks.coderaider.gundam.core.mcp.StdioMcpServerClient;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
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
 * - mcpServerCommand: MCP server command (default: "python src/main/java/.../simple_mcp_server_stdio.py")
 * 
 * Prerequisites:
 * 1. Install mcp package: pip install mcp[cli]
 * 2. Run this example - the MCP server will be started and terminated automatically.
 */
public class Example03AgentWithMcp
{
    public static void main(String[] args)
    {
        String model = args.length > 0 ? args[0] : "Qwen/Qwen3-4B";
        String apiKey = args.length > 1 ? args[1] : System.getenv("MODEL_SCOPE_API_KEY");
        String query = args.length > 2 ? args[2] : "Find onboarding policy";
        String mcpServerCommand = args.length > 3 ? args[3] : "python src/main/java/stark/dataworks/coderaider/gundam/core/examples/simple_mcp_server_stdio.py";

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
        agentRegistry.register(new Agent(agentDef));

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(new HostedMcpTool("kb-mcp", "kb_search", mcpManager));

        ModelScopeLlmClient llmClient = new ModelScopeLlmClient(apiKey, model);
        AgentRunner runner = ExampleSupport.runnerWithPublisher(llmClient, toolRegistry, agentRegistry, null, createConsoleStreamingPublisher());

        System.out.print("Streaming output: ");
        RunResult result = runner.runStreamed(agentRegistry.get("mcp-agent").orElseThrow(), query, RunConfiguration.defaults(), ExampleSupport.noopHooks());
        System.out.println();
        System.out.println("Final output: " + result.getFinalOutput());
        
        mcpClient.disconnect(mcpServer);
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        RunEventPublisher publisher = new RunEventPublisher();
        publisher.subscribe(new IRunEventListener()
        {
            @Override
            public void onEvent(RunEvent event)
            {
                if (event.getType() == RunEventType.MODEL_RESPONSE_DELTA)
                {
                    String delta = (String) event.getAttributes().get("delta");
                    if (delta != null)
                    {
                        System.out.print(delta);
                        System.out.flush();
                    }
                }
                else if (event.getType() == RunEventType.TOOL_CALL_REQUESTED)
                {
                    String tool = (String) event.getAttributes().get("tool");
                    System.out.println("\n[MCP Tool call: " + tool + "]");
                }
                else if (event.getType() == RunEventType.TOOL_CALL_COMPLETED)
                {
                    String tool = (String) event.getAttributes().get("tool");
                    System.out.println("[MCP Tool completed: " + tool + "]");
                    System.out.print("Continuing stream: ");
                }
            }
        });
        return publisher;
    }
}
