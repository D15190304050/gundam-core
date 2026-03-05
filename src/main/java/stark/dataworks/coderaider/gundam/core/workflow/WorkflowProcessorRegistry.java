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
     * Registers this value for later lookup and execution.
     * @param type type discriminator.
     * @param processor processor.
     */
    public void register(String type, IWorkflowVertexProcessor processor)
    {
        processors.put(Objects.requireNonNull(type, "type"), Objects.requireNonNull(processor, "processor"));
    }

    /**
     * Returns the current value for this object.
     * @param type type discriminator.
     * @return Optional iworkflow vertex processor value.
     */
    public Optional<IWorkflowVertexProcessor> get(String type)
    {
        return Optional.ofNullable(processors.get(type));
    }
}
