package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.builtin.WorkflowTool;
import stark.dataworks.coderaider.gundam.core.workflow.WorkflowDefinition;
import stark.dataworks.coderaider.gundam.core.workflow.WorkflowExecutor;
import stark.dataworks.coderaider.gundam.core.workflow.WorkflowProcessorRegistry;
import stark.dataworks.coderaider.gundam.core.workflow.WorkflowVertexDefinition;
import stark.dataworks.coderaider.gundam.core.workflow.processor.AgentWorkflowProcessor;
import stark.dataworks.coderaider.gundam.core.workflow.processor.JoinFieldsWorkflowProcessor;

import java.util.List;
import java.util.Map;

/**
 * 28) How to execute an agent inside a workflow, then let another agent call that workflow as a tool.
 */
public class Example28AgentInWorkflowThenCalledByAnotherAgentTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.exit(1);
        }

        AgentDefinition workflowStepAgent = new AgentDefinition();
        workflowStepAgent.setId("workflow-step-agent");
        workflowStepAgent.setName("Workflow Step Agent");
        workflowStepAgent.setModel(model);
        workflowStepAgent.setSystemPrompt("You produce concise implementation notes for a project plan.");

        AgentDefinition orchestratorAgent = new AgentDefinition();
        orchestratorAgent.setId("workflow-orchestrator-agent");
        orchestratorAgent.setName("Workflow Orchestrator Agent");
        orchestratorAgent.setModel(model);
        orchestratorAgent.setSystemPrompt("You must call tool 'workflow_plan_tool' exactly once, then return a final concise response based on the tool output.");
        orchestratorAgent.setToolNames(List.of("workflow_plan_tool"));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(workflowStepAgent);
        agentRegistry.register(orchestratorAgent);

        ToolRegistry toolRegistry = new ToolRegistry();

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(createConsoleStreamingPublisher())
            .build();

        WorkflowProcessorRegistry workflowProcessorRegistry = new WorkflowProcessorRegistry();
        workflowProcessorRegistry.register("agent", new AgentWorkflowProcessor(runner, RunConfiguration.defaults()));
        workflowProcessorRegistry.register("join", new JoinFieldsWorkflowProcessor());

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId("wf-agent-step");
        definition.setName("Workflow With Agent Step");
        definition.setStartVertexId("agent-step");
        definition.setFinalOutputKey("finalOutput");
        definition.setEndVertexIds(List.of("finalize"));

        WorkflowVertexDefinition agentStep = new WorkflowVertexDefinition();
        agentStep.setId("agent-step");
        agentStep.setProcessorType("agent");
        agentStep.setConfig(Map.of(
            "agentId", "workflow-step-agent",
            "promptTemplate", "Create implementation notes for this topic: {{topic}}",
            "outputKey", "agentNotes"));
        agentStep.setNextVertexIds(List.of("finalize"));

        WorkflowVertexDefinition finalize = new WorkflowVertexDefinition();
        finalize.setId("finalize");
        finalize.setProcessorType("join");
        finalize.setConfig(Map.of(
            "fields", List.of("agentNotes"),
            "separator", "\n",
            "outputKey", "finalOutput"));

        definition.setVertices(List.of(agentStep, finalize));

        toolRegistry.register(new WorkflowTool(
            new ToolDefinition(
                "workflow_plan_tool",
                "Execute a workflow that includes an agent step and return the final plan.",
                List.of(new ToolParameterSchema("topic", "string", true, "Project planning topic"))),
            new WorkflowExecutor(definition, workflowProcessorRegistry)));

        System.out.print("Streaming output: ");
        String output = runner.chatClient("workflow-orchestrator-agent")
            .prompt()
            .user("Please prepare a rollout plan for introducing approval gates in CI/CD.")
            .runConfiguration(RunConfiguration.defaults())
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .content();
        System.out.println();
        System.out.println("Final output: " + output);
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        return ExampleStreamingPublishers.textWithToolLifecycle("");
    }
}
