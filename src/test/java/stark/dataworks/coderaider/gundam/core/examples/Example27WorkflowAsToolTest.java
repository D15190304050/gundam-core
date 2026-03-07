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
import stark.dataworks.coderaider.gundam.core.workflow.processor.JoinFieldsWorkflowProcessor;
import stark.dataworks.coderaider.gundam.core.workflow.processor.TemplateRenderWorkflowProcessor;

import java.util.List;
import java.util.Map;

/**
 * 27) How to use workflow DAG as a tool for an agent, with streaming output.
 */
public class Example27WorkflowAsToolTest
{
    @Test
    public void runWorkflowToolWithHardcodedWorkflow()
    {
        runWithWorkflow(buildHardcodedWorkflow(), "Please call the workflow tool first, then provide the final task plan. Topic: launch a new refund policy.");
    }

    @Test
    public void runWorkflowToolWithJsonWorkflow()
    {
        String json = """
            {
              "id": "wf-json-planner",
              "name": "JSON Planner Workflow",
              "startVertexId": "prepare",
              "finalOutputKey": "finalOutput",
              "endVertexIds": ["finalize"],
              "failureStrategy": "FAIL_FAST",
              "vertices": [
                {
                  "id": "prepare",
                  "processorType": "template",
                  "config": {
                    "template": "Background: {{topic}}",
                    "outputKey": "line1"
                  },
                  "nextVertexIds": ["analyze"]
                },
                {
                  "id": "analyze",
                  "processorType": "template",
                  "config": {
                    "template": "Breakdown: user value, risks, and rollout steps",
                    "outputKey": "line2"
                  },
                  "nextVertexIds": ["finalize"]
                },
                {
                  "id": "finalize",
                  "processorType": "join",
                  "config": {
                    "fields": ["line1", "line2"],
                    "separator": "\\n",
                    "outputKey": "finalOutput"
                  }
                }
              ]
            }
            """;

        runWithWorkflow(WorkflowDefinition.fromJson(json), "Please call the workflow tool to process this topic: build a multi-region disaster recovery drill plan. Then give me final recommendations.");
    }

    private void runWithWorkflow(WorkflowDefinition workflowDefinition, String prompt)
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();
        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.exit(1);
        }

        WorkflowProcessorRegistry workflowRegistry = new WorkflowProcessorRegistry();
        workflowRegistry.register("template", new TemplateRenderWorkflowProcessor());
        workflowRegistry.register("join", new JoinFieldsWorkflowProcessor());

        AgentDefinition agentDef = new AgentDefinition();
        agentDef.setId("workflow-agent");
        agentDef.setName("Workflow Agent");
        agentDef.setModel(model);
        agentDef.setSystemPrompt("You MUST call tool 'plan_with_workflow' exactly once with the topic from user input. Then provide a final concise answer based on tool output.");
        agentDef.setToolNames(List.of("plan_with_workflow"));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(agentDef);

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(new WorkflowTool(
            new ToolDefinition(
                "plan_with_workflow",
                "Process the topic through a DAG workflow and return final planning text.",
                List.of(new ToolParameterSchema("topic", "string", true, "Topic to process through workflow"))),
            new WorkflowExecutor(workflowDefinition, workflowRegistry)));

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(createConsoleStreamingPublisher())
            .build();

        System.out.print("Streaming output: ");
        String output = runner.chatClient("workflow-agent")
            .prompt()
            .user(prompt)
            .runConfiguration(RunConfiguration.defaults())
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .content();
        System.out.println();
        System.out.println("Final output: " + output);
    }

    private WorkflowDefinition buildHardcodedWorkflow()
    {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId("wf-hardcoded-planner");
        definition.setName("Hardcoded Planner Workflow");
        definition.setStartVertexId("collect");
        definition.setFinalOutputKey("finalOutput");
        definition.setEndVertexIds(List.of("merge"));

        WorkflowVertexDefinition collect = new WorkflowVertexDefinition();
        collect.setId("collect");
        collect.setName("Collect");
        collect.setProcessorType("template");
        collect.setConfig(Map.of("template", "Topic: {{topic}}", "outputKey", "line1"));
        collect.setNextVertexIds(List.of("split"));

        WorkflowVertexDefinition split = new WorkflowVertexDefinition();
        split.setId("split");
        split.setName("Split");
        split.setProcessorType("template");
        split.setConfig(Map.of("template", "Step breakdown: goal definition -> solution design -> acceptance and retrospective", "outputKey", "line2"));
        split.setNextVertexIds(List.of("merge"));

        WorkflowVertexDefinition merge = new WorkflowVertexDefinition();
        merge.setId("merge");
        merge.setName("Merge");
        merge.setProcessorType("join");
        merge.setConfig(Map.of("fields", List.of("line1", "line2"), "separator", "\n", "outputKey", "finalOutput"));

        definition.setVertices(List.of(collect, split, merge));
        definition.validate();
        return definition;
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        return ExampleStreamingPublishers.textWithToolLifecycle("");
    }
}
