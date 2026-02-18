package stark.dataworks.coderaider.gundam.core.tracing.processor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import stark.dataworks.coderaider.gundam.core.tracing.TraceEvent;
/**
 * Class TracingProcessors.
 */

public class TracingProcessors
{
    /**
     * Field processors.
     */
    private final List<TracingProcessor> processors = new CopyOnWriteArrayList<>();
    /**
     * Executes add.
     */

    public void add(TracingProcessor processor)
    {
        processors.add(processor);
    }
    /**
     * Executes emit.
     */

    public void emit(TraceEvent event)
    {
        processors.forEach(p -> p.process(event));
    }
}
