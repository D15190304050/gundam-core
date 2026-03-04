package stark.dataworks.coderaider.gundam.core.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * WorkflowExecutor implements workflow DAG execution.
 */
public class WorkflowExecutor
{
    /**
     * Immutable definition object that configures this runtime instance.
     */
    private final WorkflowDefinition definition;

    /**
     * Registry that resolves workflow processors by type.
     */
    private final WorkflowProcessorRegistry processorRegistry;

    /**
     * Performs workflow executor as part of WorkflowExecutor runtime responsibilities.
     * @param definition The definition used by this operation.
     * @param processorRegistry The processor registry used by this operation.
     */
    public WorkflowExecutor(WorkflowDefinition definition, WorkflowProcessorRegistry processorRegistry)
    {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.definition.validate();
        this.processorRegistry = Objects.requireNonNull(processorRegistry, "processorRegistry");
    }

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param input The input used by this operation.
     * @return The value produced by this operation.
     */
    public WorkflowExecutionResult execute(Map<String, Object> input)
    {
        Map<String, WorkflowVertexDefinition> vertexIndex = definition.getVertices().stream()
            .collect(Collectors.toMap(WorkflowVertexDefinition::getId, v -> v, (a, b) -> a, LinkedHashMap::new));

        Map<String, Integer> inDegrees = buildInDegreeMap(vertexIndex);
        Map<String, Object> state = new HashMap<>(Objects.requireNonNullElse(input, Map.of()));
        List<String> completedVertices = new ArrayList<>();

        List<String> currentLayer = List.of(definition.getStartVertexId());
        while (!currentLayer.isEmpty())
        {
            Map<String, WorkflowVertexResult> layerResults = executeLayerInParallel(currentLayer, state, vertexIndex);

            Set<String> nextLayer = new LinkedHashSet<>();
            for (String vertexId : currentLayer)
            {
                WorkflowVertexResult result = layerResults.get(vertexId);
                if (result == null)
                {
                    throw new IllegalStateException("Missing execution result for vertex: " + vertexId);
                }
                completedVertices.add(vertexId);
                state.putAll(result.getOutputs());

                List<String> pointedVertices = vertexIndex.get(vertexId).getNextVertexIds() == null
                    ? List.of()
                    : vertexIndex.get(vertexId).getNextVertexIds();
                for (String pointedVertexId : pointedVertices)
                {
                    int nextInDegree = inDegrees.get(pointedVertexId) - 1;
                    inDegrees.put(pointedVertexId, nextInDegree);
                    if (nextInDegree == 0)
                    {
                        nextLayer.add(pointedVertexId);
                    }
                }
            }

            currentLayer = new ArrayList<>(nextLayer);
        }

        if (completedVertices.size() != vertexIndex.size())
        {
            throw new IllegalStateException("Workflow execution incomplete. Completed vertices: " + completedVertices.size()
                + ", total vertices: " + vertexIndex.size());
        }

        Object finalOutputValue = state.getOrDefault(definition.getFinalOutputKey(), "");
        return new WorkflowExecutionResult(String.valueOf(finalOutputValue), state, completedVertices);
    }

    private Map<String, Integer> buildInDegreeMap(Map<String, WorkflowVertexDefinition> vertexIndex)
    {
        Map<String, Integer> inDegrees = new HashMap<>();
        for (String vertexId : vertexIndex.keySet())
        {
            inDegrees.put(vertexId, 0);
        }
        for (WorkflowVertexDefinition vertex : vertexIndex.values())
        {
            List<String> pointedVertices = vertex.getNextVertexIds() == null ? List.of() : vertex.getNextVertexIds();
            for (String pointedVertex : pointedVertices)
            {
                inDegrees.put(pointedVertex, inDegrees.get(pointedVertex) + 1);
            }
        }
        return inDegrees;
    }

    private Map<String, WorkflowVertexResult> executeLayerInParallel(List<String> layerVertexIds, Map<String, Object> state,
        Map<String, WorkflowVertexDefinition> vertexIndex)
    {
        Map<String, WorkflowVertexResult> results = new HashMap<>();
        Map<String, Object> layerStateSnapshot = Collections.unmodifiableMap(new HashMap<>(state));

        try (ExecutorService executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor())
        {
            ExecutorCompletionService<VertexExecutionOutcome> completionService = new ExecutorCompletionService<>(executor);
            List<Future<VertexExecutionOutcome>> futures = new ArrayList<>();
            for (String vertexId : layerVertexIds)
            {
                WorkflowVertexDefinition vertex = vertexIndex.get(vertexId);
                futures.add(completionService.submit(runVertex(vertexId, vertex, layerStateSnapshot)));
            }

            for (int i = 0; i < layerVertexIds.size(); i++)
            {
                VertexExecutionOutcome outcome = completionService.take().get();
                results.put(outcome.vertexId(), outcome.result());
            }

            for (Future<VertexExecutionOutcome> future : futures)
            {
                if (!future.isDone())
                {
                    future.cancel(true);
                }
            }
        }
        catch (Exception ex)
        {
            if (ex.getCause() instanceof RuntimeException runtimeException)
            {
                throw runtimeException;
            }
            throw new IllegalStateException("Failed to execute workflow layer", ex);
        }

        return results;
    }

    private Callable<VertexExecutionOutcome> runVertex(String vertexId, WorkflowVertexDefinition vertex, Map<String, Object> state)
    {
        return () -> new VertexExecutionOutcome(vertexId, executeVertexWithRetry(vertex, state));
    }

    private WorkflowVertexResult executeVertexWithRetry(WorkflowVertexDefinition vertex, Map<String, Object> state)
    {
        int maxAttempts = Math.max(1, vertex.getMaxRetries() + 1);
        RuntimeException latestFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++)
        {
            try
            {
                IWorkflowVertexProcessor processor = processorRegistry.get(vertex.getProcessorType())
                    .orElseThrow(() -> new IllegalStateException("Workflow processor not found: " + vertex.getProcessorType()));
                return processor.process(vertex, state);
            }
            catch (RuntimeException ex)
            {
                latestFailure = ex;
                if (attempt >= maxAttempts)
                {
                    boolean continueOnFailure = vertex.isContinueOnFailure() || definition.getFailureStrategy() == WorkflowFailureStrategy.CONTINUE;
                    if (continueOnFailure)
                    {
                        return WorkflowVertexResult.ofOutputs(Map.of("workflowError_" + vertex.getId(), ex.getMessage()));
                    }
                }
            }
        }

        throw new IllegalStateException("Unexpected workflow execution state", latestFailure);
    }

    private record VertexExecutionOutcome(String vertexId, WorkflowVertexResult result)
    {
    }
}
