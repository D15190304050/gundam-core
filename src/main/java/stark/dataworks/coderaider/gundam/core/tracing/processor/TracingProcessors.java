package stark.dataworks.coderaider.gundam.core.tracing.processor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import stark.dataworks.coderaider.gundam.core.tracing.TraceEvent;

/**
 * TracingProcessors implements run tracing and span publication.
 */
public class TracingProcessors
{

    /**
 * Registered tracing processors invoked for each run event.
     */
    private final List<ITracingProcessor> processors = new CopyOnWriteArrayList<>();

    /**
     * Adds this value.
     * @param processor processor.
     */
    public void add(ITracingProcessor processor)
    {
        processors.add(processor);
    }

    /**
     * Emits a trace event to registered processors.
     * @param event run event.
     */
    public void emit(TraceEvent event)
    {
        processors.forEach(p -> p.process(event));
    }
}
