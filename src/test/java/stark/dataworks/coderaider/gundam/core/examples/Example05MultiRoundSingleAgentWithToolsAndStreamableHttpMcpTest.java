package stark.dataworks.coderaider.gundam.core.examples;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.time.Duration;
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
import stark.dataworks.coderaider.gundam.core.mcp.StreamableHttpMcpServerClient;
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
 * 5) How to create a multi-round single agent with tools and MCPs (Streamable HTTP transport), with streaming output.
 *
 * Usage: java Example05MultiRoundSingleAgentWithToolsAndStreamableHttpMcp [model] [apiKey] [mcpServerUrl]
 * - model: ModelScope model name (default: Qwen/Qwen3-4B)
 * - apiKey: Your ModelScope API key (required, or set MODEL_SCOPE_API_KEY env var)
 * - mcpServerUrl: MCP server endpoint (default: "http://localhost:8766/mcp")
 *
 * Prerequisites:
 * 1. Install mcp package: pip install mcp[cli]
 * 2. Start the Streamable HTTP MCP server first: python src/main/resources/mcp/simple_mcp_server_streamable_http.py
 * 3. Run this example - it will connect to the Streamable HTTP MCP server.
 */
public class Example05MultiRoundSingleAgentWithToolsAndStreamableHttpMcpTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        String mcpServerUrl = "http://localhost:8766/mcp";
        Process mcpProcess = McpTestSupport.startPythonScript("src/main/resources/mcp/simple_mcp_server_streamable_http.py", "8766");
        McpTestSupport.waitForHttp(mcpServerUrl, Duration.ofSeconds(10));

        if (apiKey == null || apiKey.isBlank())
        {
            McpTestSupport.stopQuietly(mcpProcess);
            System.err.println("Error: ModelScope API key is required.");
            System.err.println("Set MODEL_SCOPE_API_KEY environment variable or pass as second argument.");
            System.exit(1);
        }

        try
        {
            StreamableHttpMcpServerClient mcpClient = new StreamableHttpMcpServerClient();
            McpManager mcpManager = new McpManager(mcpClient);

            McpServerConfiguration mcpServer = new McpServerConfiguration("policy-mcp-streamable-http", mcpServerUrl, Map.of());
            mcpManager.registerServer(mcpServer);

            List<McpToolDescriptor> mcpTools = mcpClient.listTools(mcpServer);
            System.out.println("Available MCP tools: " + mcpTools.stream().map(McpToolDescriptor::getName).toList());

            AgentDefinition agentDef = new AgentDefinition();
            agentDef.setId("hybrid-streamable-http-agent");
            agentDef.setName("Hybrid Streamable HTTP Agent");
            agentDef.setModel(model);
            agentDef.setSystemPrompt("You are a helpful assistant with access to tax calculation and policy lookup tools. Use them when appropriate.");
            agentDef.setToolNames(List.of("tax_calculator", "policy_lookup"));

            AgentRegistry agentRegistry = new AgentRegistry();
            agentRegistry.register(agentDef);

            ToolRegistry toolRegistry = new ToolRegistry();
            toolRegistry.register(createTaxCalculatorTool());
            toolRegistry.register(new HostedMcpTool("policy-mcp-streamable-http", "policy_lookup", mcpManager));

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
            ContextResult round1 = runner.chatClient("hybrid-streamable-http-agent").prompt().user("Please estimate tax for amount 100.").runConfiguration(config).runHooks(ExampleSupport.noopHooks()).call().contextResult();
            System.out.println();
            System.out.println("Round 1 output: " + round1.getFinalOutput());

            System.out.println("\n=== Round 2: Policy Constraints ===");
            System.out.print("Streaming output: ");
            ContextResult round2 = runner.chatClient("hybrid-streamable-http-agent").prompt().user("What policy constraints should I know about tax?").runConfiguration(config).runHooks(ExampleSupport.noopHooks()).call().contextResult();
            System.out.println();
            System.out.println("Round 2 output: " + round2.getFinalOutput());
        }
        finally
        {
            McpTestSupport.stopQuietly(mcpProcess);
        }
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        return ExampleStreamingPublishers.textWithToolLifecycle("MCP ");
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
