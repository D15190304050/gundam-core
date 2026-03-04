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
     * Unique identifier for this definition.
     */
    private String id;

    /**
     * Human-readable name used in logs and UIs.
     */
    private String name;

    /**
     * Processor key used to resolve vertex execution logic.
     */
    private String processorType;

    /**
     * Processor configuration map for this workflow vertex.
     */
    private Map<String, Object> config = new HashMap<>();

    /**
     * Ordered list of next vertex ids.
     */
    private List<String> nextVertexIds = new ArrayList<>();

    /**
     * Maximum retry attempts for this vertex after a failure.
     */
    private int maxRetries;

    /**
     * Whether workflow execution continues when this vertex fails.
     */
    private boolean continueOnFailure;
}
