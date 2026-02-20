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
import stark.dataworks.coderaider.gundam.core.session.InMemorySessionStore;
import stark.dataworks.coderaider.gundam.core.streaming.IRunEventListener;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.builtin.mcp.HostedMcpTool;

/**
 * 4) How to create a multi-round single agent with tools and MCPs, with streaming output.
 * 
 * Usage: java Example04MultiRoundSingleAgentWithToolsAndMcp [model] [apiKey] [mcpServerCommand]
 * - model: ModelScope model name (default: Qwen/Qwen3-4B)
 * - apiKey: Your ModelScope API key (required, or set MODEL_SCOPE_API_KEY env var)
 * - mcpServerCommand: MCP server command (default: "python src/main/java/.../simple_mcp_server_stdio.py")
 * 
 * Prerequisites:
 * 1. Install mcp package: pip install mcp[cli]
 * 2. Run this example - the MCP server will be started and terminated automatically.
 * 
 * This example demonstrates a multi-round conversation with a single agent
 * that has access to both regular tools and MCP tools.
 */
public class Example04MultiRoundSingleAgentWithToolsAndMcp
{
    public static void main(String[] args)
    {
        String model = args.length > 0 ? args[0] : "Qwen/Qwen3-4B";
        String apiKey = args.length > 1 ? args[1] : System.getenv("MODEL_SCOPE_API_KEY");
        String mcpServerCommand = args.length > 2 ? args[2] : "python src/main/java/stark/dataworks/coderaider/gundam/core/examples/simple_mcp_server_stdio.py";

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.err.println("Set MODEL_SCOPE_API_KEY environment variable or pass as second argument.");
            System.exit(1);
        }

        StdioMcpServerClient mcpClient = new StdioMcpServerClient();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[MCP] Shutting down MCP server...");
            mcpClient.disconnect(new McpServerConfiguration("policy-mcp", mcpServerCommand, Map.of()));
        }));

        McpManager mcpManager = new McpManager(mcpClient);

        McpServerConfiguration mcpServer = new McpServerConfiguration(
            "policy-mcp",
            mcpServerCommand,
            Map.of());
        mcpManager.registerServer(mcpServer);

        List<McpToolDescriptor> mcpTools = mcpClient.listTools(mcpServer);
        System.out.println("Available MCP tools: " + mcpTools.stream().map(McpToolDescriptor::getName).toList());

        AgentDefinition agentDef = new AgentDefinition();
        agentDef.setId("hybrid-agent");
        agentDef.setName("Hybrid Agent");
        agentDef.setModel(model);
        agentDef.setSystemPrompt("You are a helpful assistant with access to tax calculation and policy lookup tools. Use them when appropriate.");
        agentDef.setToolNames(List.of("tax_calculator", "policy_lookup"));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(agentDef));

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(createTaxCalculatorTool());
        toolRegistry.register(new HostedMcpTool("policy-mcp", "policy_lookup", mcpManager));

        InMemorySessionStore sessionStore = new InMemorySessionStore();
        RunConfiguration config = RunConfiguration.defaults();

        ModelScopeLlmClient llmClient = new ModelScopeLlmClient(apiKey, model);
        AgentRunner runner = ExampleSupport.runnerWithPublisher(llmClient, toolRegistry, agentRegistry, sessionStore, createConsoleStreamingPublisher());

        System.out.println("=== Round 1: Tax Estimation ===");
        System.out.print("Streaming output: ");
        RunResult round1 = runner.runStreamed(agentRegistry.get("hybrid-agent").orElseThrow(), "Please estimate tax for amount 100.", config, ExampleSupport.noopHooks());
        System.out.println();
        System.out.println("Round 1 output: " + round1.getFinalOutput());

        System.out.println("\n=== Round 2: Policy Constraints ===");
        System.out.print("Streaming output: ");
        RunResult round2 = runner.runStreamed(agentRegistry.get("hybrid-agent").orElseThrow(), "What policy constraints should I know?", config, ExampleSupport.noopHooks());
        System.out.println();
        System.out.println("Round 2 output: " + round2.getFinalOutput());

        System.out.println("\n=== Round 3: Combined Query ===");
        System.out.print("Streaming output: ");
        RunResult round3 = runner.runStreamed(agentRegistry.get("hybrid-agent").orElseThrow(), "Calculate tax for 500 and check relevant policies.", config, ExampleSupport.noopHooks());
        System.out.println();
        System.out.println("Round 3 output: " + round3.getFinalOutput());
        
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
                    System.out.println("\n[Tool call: " + tool + "]");
                }
                else if (event.getType() == RunEventType.TOOL_CALL_COMPLETED)
                {
                    String tool = (String) event.getAttributes().get("tool");
                    System.out.println("[Tool completed: " + tool + "]");
                    System.out.print("Continuing stream: ");
                }
            }
        });
        return publisher;
    }

    private static ITool createTaxCalculatorTool()
    {
        return new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition(
                    "tax_calculator",
                    "Calculate estimated tax for a given amount",
                    List.of(
                        new ToolParameterSchema("amount", "number", true, "The amount to calculate tax for")
                    ));
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                double amount = ((Number) input.getOrDefault("amount", 0)).doubleValue();
                double tax = amount * 0.1;
                return String.format("{\"amount\": %.2f, \"tax\": %.2f, \"total\": %.2f}", amount, tax, amount + tax);
            }
        };
    }
}
