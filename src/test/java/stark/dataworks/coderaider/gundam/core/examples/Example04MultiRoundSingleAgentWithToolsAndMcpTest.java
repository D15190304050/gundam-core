package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.event.RunEventType;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.mcp.SseMcpServerClient;
import stark.dataworks.coderaider.gundam.core.mcp.McpManager;
import stark.dataworks.coderaider.gundam.core.mcp.McpServerConfiguration;
import stark.dataworks.coderaider.gundam.core.mcp.McpToolDescriptor;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
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
 * 4) How to create a multi-round single agent with tools and MCPs (HTTP transport), with streaming output.
 * 
 * Usage: java Example04MultiRoundSingleAgentWithToolsAndMcp [model] [apiKey] [mcpServerUrl]
 * - model: ModelScope model name (default: Qwen/Qwen3-4B)
 * - apiKey: Your ModelScope API key (required, or set MODEL_SCOPE_API_KEY env var)
 * - mcpServerUrl: MCP server SSE endpoint (default: "http://localhost:8765/sse")
 * 
 * Prerequisites:
 * 1. Install mcp package: pip install mcp[cli]
 * 2. Start the HTTP MCP server first: python src/main/resources/mcp/simple_mcp_server_http.py
 * 3. Run this example - it will connect to the SSE MCP server.
 * 
 * This example demonstrates a multi-round conversation with a single agent
 * that has access to both regular tools and MCP tools via HTTP transport.
 */
public class Example04MultiRoundSingleAgentWithToolsAndMcpTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        String mcpServerUrl = "http://localhost:8765/sse";

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.err.println("Set MODEL_SCOPE_API_KEY environment variable or pass as second argument.");
            System.exit(1);
        }

        SseMcpServerClient mcpClient = new SseMcpServerClient();

        McpManager mcpManager = new McpManager(mcpClient);

        McpServerConfiguration mcpServer = new McpServerConfiguration(
            "policy-mcp",
            mcpServerUrl,
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

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .sessionStore(sessionStore)
            .eventPublisher(createConsoleStreamingPublisher())
            .build();

        System.out.println("=== Round 1: Tax Estimation ===");
        System.out.print("Streaming output: ");
        ContextResult round1 = runner.runStreamed(agentRegistry.get("hybrid-agent").orElseThrow(), "Please estimate tax for amount 100.", config, ExampleSupport.noopHooks());
        System.out.println();
        System.out.println("Round 1 output: " + round1.getFinalOutput());

        System.out.println("\n=== Round 2: Policy Constraints ===");
        System.out.print("Streaming output: ");
        ContextResult round2 = runner.runStreamed(agentRegistry.get("hybrid-agent").orElseThrow(), "What policy constraints should I know about tax?", config, ExampleSupport.noopHooks());
        System.out.println();
        System.out.println("Round 2 output: " + round2.getFinalOutput());

        System.out.println("\n=== Round 3: Combined Query ===");
        System.out.print("Streaming output: ");
        ContextResult round3 = runner.runStreamed(agentRegistry.get("hybrid-agent").orElseThrow(), "Calculate tax for 500 and check relevant policies.", config, ExampleSupport.noopHooks());
        System.out.println();
        System.out.println("Round 3 output: " + round3.getFinalOutput());
        
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
