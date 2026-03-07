package stark.dataworks.coderaider.gundam.core.examples;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import io.github.cdimascio.dotenv.Dotenv;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.computer.Environment;
import stark.dataworks.coderaider.gundam.core.computer.SimulatedComputer;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.builtin.ComputerTool;

/**
 * Example demonstrating ComputerTool with Agent + AgentRunner orchestration.
 */
public class Example23ComputerToolTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));

        if (apiKey == null || apiKey.isBlank())
        {
            System.out.println("Skipping test: MODEL_SCOPE_API_KEY not set");
            return;
        }

        String model = "Qwen/Qwen3-4B";

        System.out.println("=== ComputerTool Agent Example ===");

        SimulatedComputer computer = new SimulatedComputer(Environment.BROWSER, 1024, 768);
        ComputerTool computerTool = new ComputerTool(computer);

        AgentDefinition agentDefinition = new AgentDefinition();
        agentDefinition.setId("computer-agent");
        agentDefinition.setName("Computer Agent");
        agentDefinition.setModel(model);
        agentDefinition.setSystemPrompt("You are a computer-use assistant. Use computer_use_preview for computer actions.");
        agentDefinition.setToolNames(List.of(computerTool.definition().getName()));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(agentDefinition);

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(computerTool);

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(ExampleStreamingPublishers.textWithToolLifecycle(""))
            .build();

        ContextResult result = runner.chatClient("computer-agent")
            .prompt()
            .user("Use computer_use_preview to take one screenshot, then left-click at coordinates (100, 200).")
            .runConfiguration(RunConfiguration.defaults())
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .contextResult();

        System.out.println("\nFinal output: " + result.getFinalOutput());
        System.out.println("\n=== Action Log ===");

        assumeFalse(result.getFinalOutput().startsWith("Run failed:"),
            "Skipping assertions due to provider/network failure: " + result.getFinalOutput());

        List<SimulatedComputer.ComputerAction> actions = computer.getActionLog();
        for (int i = 0; i < actions.size(); i++)
        {
            System.out.println((i + 1) + ". " + actions.get(i));
        }

        System.out.println("\n=== Screenshot Count ===");
        System.out.println("Total screenshots: " + computer.getScreenshotCount());

        assertEquals("computer-agent", result.getFinalAgentId());
        assertTrue(actions.stream().anyMatch(action -> "screenshot".equals(action.getAction())));
        assertTrue(actions.stream().anyMatch(action -> "click".equals(action.getAction())
            && action.getX() == 100
            && action.getY() == 200));
        assertTrue(computer.getScreenshotCount() >= 1);
        assertTrue(result.getFinalOutput() != null && !result.getFinalOutput().isBlank());

        System.out.println("\nExample completed successfully!");
    }
}
