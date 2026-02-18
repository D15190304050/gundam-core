package stark.dataworks.coderaider.gundam.core.tracing.processor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import stark.dataworks.coderaider.gundam.core.tracing.TraceEvent;

/**
 * TracingProcessors implements run tracing and span publication.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class TracingProcessors
{

    /**
     * Internal state for processors used while coordinating runtime behavior.
     */
    private final List<TracingProcessor> processors = new CopyOnWriteArrayList<>();

    /**
     * Adds data to internal state consumed by later runtime steps.
     * @param processor The processor used by this operation.
     */
    public void add(TracingProcessor processor)
    {
        processors.add(processor);
    }

    /**
     * Publishes a runtime event so hooks/listeners can observe progress.
     * @param event The event used by this operation.
     */
    public void emit(TraceEvent event)
    {
        processors.forEach(p -> p.process(event));
    }
}
