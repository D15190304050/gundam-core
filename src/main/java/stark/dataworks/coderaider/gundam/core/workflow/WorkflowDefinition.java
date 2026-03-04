package stark.dataworks.coderaider.gundam.core.workflow;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * WorkflowDefinition implements workflow DAG configuration.
 */
@Getter
@Setter
public class WorkflowDefinition
{
    /**
     * Internal state for id; used while coordinating runtime behavior.
     */
    private String id;

    /**
     * Internal state for name; used while coordinating runtime behavior.
     */
    private String name;

    /**
     * Internal state for start vertex id; used while coordinating runtime behavior.
     */
    private String startVertexId;

    /**
     * Internal state for end vertex ids; used while coordinating runtime behavior.
     */
    private List<String> endVertexIds = new ArrayList<>();

    /**
     * Internal state for final output key; used while coordinating runtime behavior.
     */
    private String finalOutputKey = "finalOutput";

    /**
     * Internal state for failure strategy; used while coordinating runtime behavior.
     */
    private WorkflowFailureStrategy failureStrategy = WorkflowFailureStrategy.FAIL_FAST;

    /**
     * Internal state for vertices; used while coordinating runtime behavior.
     */
    private List<WorkflowVertexDefinition> vertices = new ArrayList<>();

    /**
     * Performs validate as part of WorkflowDefinition runtime responsibilities.
     */
    public void validate()
    {
        if (id == null || id.isBlank())
        {
            throw new IllegalArgumentException("Workflow id is required");
        }
        if (startVertexId == null || startVertexId.isBlank())
        {
            throw new IllegalArgumentException("Workflow startVertexId is required");
        }
        if (endVertexIds == null || endVertexIds.isEmpty())
        {
            throw new IllegalArgumentException("Workflow endVertexIds are required");
        }
        if (vertices.isEmpty())
        {
            throw new IllegalArgumentException("Workflow vertices are required");
        }

        Map<String, WorkflowVertexDefinition> index = vertices.stream()
            .collect(Collectors.toMap(WorkflowVertexDefinition::getId, v -> v));
        if (!index.containsKey(startVertexId))
        {
            throw new IllegalArgumentException("Workflow startVertexId must exist in vertices");
        }

        Map<String, Integer> inDegrees = new HashMap<>();
        for (WorkflowVertexDefinition vertex : vertices)
        {
            if (vertex.getId() == null || vertex.getId().isBlank())
            {
                throw new IllegalArgumentException("Workflow vertex id is required");
            }
            if (vertex.getProcessorType() == null || vertex.getProcessorType().isBlank())
            {
                throw new IllegalArgumentException("Workflow vertex processorType is required: " + vertex.getId());
            }
            inDegrees.put(vertex.getId(), 0);
        }

        for (WorkflowVertexDefinition vertex : vertices)
        {
            List<String> nextVertexIds = vertex.getNextVertexIds() == null ? List.of() : vertex.getNextVertexIds();
            for (String nextVertexId : nextVertexIds)
            {
                if (!index.containsKey(nextVertexId))
                {
                    throw new IllegalArgumentException("Workflow vertex references unknown nextVertexId: " + nextVertexId);
                }
                inDegrees.put(nextVertexId, inDegrees.get(nextVertexId) + 1);
            }
        }

        long zeroInDegreeCount = inDegrees.entrySet().stream().filter(e -> e.getValue() == 0).count();
        if (zeroInDegreeCount != 1 || inDegrees.getOrDefault(startVertexId, -1) != 0)
        {
            throw new IllegalArgumentException("Workflow must have exactly one start vertex with zero indegree");
        }

        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();
        if (hasCycle(startVertexId, index, visited, stack))
        {
            throw new IllegalArgumentException("Workflow must be a DAG: cycle detected");
        }
        if (visited.size() != vertices.size())
        {
            throw new IllegalArgumentException("Workflow contains unreachable vertices from startVertexId");
        }

        for (String endVertexId : endVertexIds)
        {
            WorkflowVertexDefinition endVertex = index.get(endVertexId);
            if (endVertex == null)
            {
                throw new IllegalArgumentException("Workflow endVertexId must exist in vertices: " + endVertexId);
            }
            if (endVertex.getNextVertexIds() != null && !endVertex.getNextVertexIds().isEmpty())
            {
                throw new IllegalArgumentException("Workflow end vertex must not point to next vertices: " + endVertexId);
            }
        }

        if (countTerminalVertices(vertices) < 1)
        {
            throw new IllegalArgumentException("Workflow must include at least one ending vertex");
        }
    }

    private int countTerminalVertices(List<WorkflowVertexDefinition> workflowVertices)
    {
        int count = 0;
        for (WorkflowVertexDefinition vertex : workflowVertices)
        {
            if (vertex.getNextVertexIds() == null || vertex.getNextVertexIds().isEmpty())
            {
                count++;
            }
        }
        return count;
    }

    private boolean hasCycle(String id, Map<String, WorkflowVertexDefinition> index, Set<String> visited, Set<String> stack)
    {
        if (stack.contains(id))
        {
            return true;
        }
        if (visited.contains(id))
        {
            return false;
        }

        visited.add(id);
        stack.add(id);
        WorkflowVertexDefinition vertex = index.get(id);
        List<String> nextVertexIds = vertex.getNextVertexIds() == null ? List.of() : vertex.getNextVertexIds();
        for (String nextId : nextVertexIds)
        {
            if (hasCycle(nextId, index, visited, stack))
            {
                return true;
            }
        }
        stack.remove(id);
        return false;
    }
}
