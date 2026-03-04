package stark.dataworks.coderaider.gundam.core.workflow;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * WorkflowProcessorRegistry implements workflow DAG vertex processor lookup.
 */
public class WorkflowProcessorRegistry
{
    /**
     * Registered tracing processors invoked for each run event.
     */
    private final Map<String, IWorkflowVertexProcessor> processors = new HashMap<>();

    /**
     * Performs register as part of WorkflowProcessorRegistry runtime responsibilities.
     * @param type The type used by this operation.
     * @param processor The processor used by this operation.
     */
    public void register(String type, IWorkflowVertexProcessor processor)
    {
        processors.put(Objects.requireNonNull(type, "type"), Objects.requireNonNull(processor, "processor"));
    }

    /**
     * Performs get as part of WorkflowProcessorRegistry runtime responsibilities.
     * @param type The type used by this operation.
     * @return The value produced by this operation.
     */
    public Optional<IWorkflowVertexProcessor> get(String type)
    {
        return Optional.ofNullable(processors.get(type));
    }
}
