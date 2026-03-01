package stark.dataworks.coderaider.gundam.core.examples;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.computer.Environment;
import stark.dataworks.coderaider.gundam.core.computer.SimulatedComputer;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
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
        System.out.println("=== ComputerTool Agent Example ===");

        SimulatedComputer computer = new SimulatedComputer(Environment.BROWSER, 1024, 768);
        ComputerTool computerTool = new ComputerTool(computer);

        AgentDefinition agentDefinition = new AgentDefinition();
        agentDefinition.setId("computer-agent");
        agentDefinition.setName("Computer Agent");
        agentDefinition.setModel("scripted-computer-model");
        agentDefinition.setSystemPrompt("You are a computer-use assistant. Use computer_use_preview to perform actions.");
        agentDefinition.setToolNames(List.of(computerTool.definition().getName()));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(agentDefinition));

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(computerTool);

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ScriptedComputerLlmClient())
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(ExampleStreamingPublishers.textWithToolLifecycle(""))
            .build();

        ContextResult result = runner.chatClient("computer-agent")
            .prompt()
            .user("Take a screenshot and click at (100, 200).")
            .runConfiguration(RunConfiguration.defaults())
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .contextResult();

        System.out.println("\nFinal output: " + result.getFinalOutput());
        System.out.println("\n=== Action Log ===");

        List<SimulatedComputer.ComputerAction> actions = computer.getActionLog();
        for (int i = 0; i < actions.size(); i++)
        {
            System.out.println((i + 1) + ". " + actions.get(i));
        }

        System.out.println("\n=== Screenshot Count ===");
        System.out.println("Total screenshots: " + computer.getScreenshotCount());

        assertEquals("computer-agent", result.getFinalAgentId());
        assertEquals(2, actions.size());
        assertEquals("screenshot", actions.get(0).getAction());
        assertEquals("click", actions.get(1).getAction());
        assertEquals(100, actions.get(1).getX());
        assertEquals(200, actions.get(1).getY());
        assertEquals(1, computer.getScreenshotCount());
        assertTrue(result.getFinalOutput().contains("Completed computer actions"));

        System.out.println("\nExample completed successfully!");
    }

    private static class ScriptedComputerLlmClient implements ILlmClient
    {
        private int callCount;

        @Override
        public LlmResponse chat(LlmRequest request)
        {
            callCount++;
            if (callCount == 1)
            {
                return new LlmResponse(
                    "",
                    List.of(
                        new ToolCall("computer_use_preview", Map.of("action", "screenshot")),
                        new ToolCall("computer_use_preview", Map.of(
                            "action", "click",
                            "x", 100,
                            "y", 200,
                            "button", "left"
                        ))
                    ),
                    null,
                    null
                );
            }

            return new LlmResponse(
                "Completed computer actions: screenshot and click at (100, 200).",
                List.of(),
                null,
                null
            );
        }
    }
}
