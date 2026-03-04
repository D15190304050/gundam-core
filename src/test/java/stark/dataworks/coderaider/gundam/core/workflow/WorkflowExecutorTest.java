package stark.dataworks.coderaider.gundam.core.workflow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.workflow.processor.JoinFieldsWorkflowProcessor;
import stark.dataworks.coderaider.gundam.core.workflow.processor.TemplateRenderWorkflowProcessor;

import java.util.List;
import java.util.Map;

public class WorkflowExecutorTest
{
    @Test
    public void shouldRunHardcodedDAG()
    {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId("wf-hardcoded");
        definition.setName("Hardcoded");
        definition.setStartVertexId("v1");
        definition.setFinalOutputKey("finalOutput");
        definition.setEndVertexIds(List.of("v3"));

        WorkflowVertexDefinition v1 = new WorkflowVertexDefinition();
        v1.setId("v1");
        v1.setProcessorType("template");
        v1.setConfig(Map.of("template", "Task: {{task}}", "outputKey", "taskLine"));
        v1.setNextVertexIds(List.of("v2"));

        WorkflowVertexDefinition v2 = new WorkflowVertexDefinition();
        v2.setId("v2");
        v2.setProcessorType("template");
        v2.setConfig(Map.of("template", "Priority: {{priority}}", "outputKey", "priorityLine"));
        v2.setNextVertexIds(List.of("v3"));

        WorkflowVertexDefinition v3 = new WorkflowVertexDefinition();
        v3.setId("v3");
        v3.setProcessorType("join");
        v3.setConfig(Map.of("fields", List.of("taskLine", "priorityLine"), "outputKey", "finalOutput", "separator", " | "));

        definition.setVertices(List.of(v1, v2, v3));

        WorkflowProcessorRegistry registry = new WorkflowProcessorRegistry();
        registry.register("template", new TemplateRenderWorkflowProcessor());
        registry.register("join", new JoinFieldsWorkflowProcessor());

        WorkflowExecutionResult result = new WorkflowExecutor(definition, registry).execute(Map.of("task", "Refactor API", "priority", "P1"));

        Assertions.assertEquals("Task: Refactor API | Priority: P1", result.getFinalOutput());
        Assertions.assertEquals(List.of("v1", "v2", "v3"), result.getCompletedVertexIds());
    }


    @Test
    public void shouldExecuteSameLayerVerticesInParallel()
    {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId("wf-parallel");
        definition.setName("Parallel");
        definition.setStartVertexId("A");
        definition.setEndVertexIds(List.of("D"));
        definition.setFinalOutputKey("finalOutput");

        WorkflowVertexDefinition a = new WorkflowVertexDefinition();
        a.setId("A");
        a.setProcessorType("template");
        a.setConfig(Map.of("template", "seed", "outputKey", "seed"));
        a.setNextVertexIds(List.of("B", "C"));

        WorkflowVertexDefinition b = new WorkflowVertexDefinition();
        b.setId("B");
        b.setProcessorType("slow");
        b.setConfig(Map.of("outputKey", "fromB"));
        b.setNextVertexIds(List.of("D"));

        WorkflowVertexDefinition c = new WorkflowVertexDefinition();
        c.setId("C");
        c.setProcessorType("slow");
        c.setConfig(Map.of("outputKey", "fromC"));
        c.setNextVertexIds(List.of("D"));

        WorkflowVertexDefinition d = new WorkflowVertexDefinition();
        d.setId("D");
        d.setProcessorType("join");
        d.setConfig(Map.of("fields", List.of("fromB", "fromC"), "separator", ",", "outputKey", "finalOutput"));

        definition.setVertices(List.of(a, b, c, d));

        WorkflowProcessorRegistry registry = new WorkflowProcessorRegistry();
        registry.register("template", new TemplateRenderWorkflowProcessor());
        registry.register("slow", (vertex, state) ->
        {
            try
            {
                Thread.sleep(400);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            return WorkflowVertexResult.ofOutputs(Map.of(String.valueOf(vertex.getConfig().get("outputKey")), vertex.getId()));
        });
        registry.register("join", new JoinFieldsWorkflowProcessor());

        long start = System.currentTimeMillis();
        WorkflowExecutionResult result = new WorkflowExecutor(definition, registry).execute(Map.of());
        long elapsed = System.currentTimeMillis() - start;

        Assertions.assertTrue(elapsed < 750, "Expected parallel layer execution, elapsed=" + elapsed);
        Assertions.assertEquals("B,C", result.getFinalOutput());
    }

    @Test
    public void shouldLoadWorkflowFromJson()
    {
        String json = """
            {
              "id": "wf-json",
              "name": "Json workflow",
              "startVertexId": "load",
              "finalOutputKey": "finalOutput",
              "endVertexIds": ["finish"],
              "vertices": [
                {
                  "id": "load",
                  "processorType": "template",
                  "config": {"template": "Input={{input}}", "outputKey": "line1"},
                  "nextVertexIds": ["finish"]
                },
                {
                  "id": "finish",
                  "processorType": "join",
                  "config": {"fields": ["line1"], "outputKey": "finalOutput"}
                }
              ]
            }
            """;

        WorkflowDefinition definition = WorkflowDefinitionLoader.fromJson(json);
        WorkflowProcessorRegistry registry = new WorkflowProcessorRegistry();
        registry.register("template", new TemplateRenderWorkflowProcessor());
        registry.register("join", new JoinFieldsWorkflowProcessor());

        WorkflowExecutionResult result = new WorkflowExecutor(definition, registry).execute(Map.of("input", "hello"));
        Assertions.assertEquals("Input=hello", result.getFinalOutput());
    }
}
