package stark.dataworks.coderaider.gundam.core.workflow;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WorkflowVertexDefinition implements workflow DAG vertex configuration.
 */
@Getter
@Setter
public class WorkflowVertexDefinition
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
     * Internal state for processor type; used while coordinating runtime behavior.
     */
    private String processorType;

    /**
     * Internal state for config; used while coordinating runtime behavior.
     */
    private Map<String, Object> config = new HashMap<>();

    /**
     * Internal state for next vertex ids; used while coordinating runtime behavior.
     */
    private List<String> nextVertexIds = new ArrayList<>();

    /**
     * Internal state for max retries; used while coordinating runtime behavior.
     */
    private int maxRetries;

    /**
     * Internal state for continue on failure; used while coordinating runtime behavior.
     */
    private boolean continueOnFailure;
}
