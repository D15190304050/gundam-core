package stark.dataworks.coderaider.gundam.core.tracing;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.tracing.processor.TracingProcessors;

/**
 * ProcessorTraceProvider implements run tracing and span publication.
 */
public class ProcessorTraceProvider implements ITraceProvider
{

    /**
     * Registered tracing processors invoked for each run event.
     */
    private final TracingProcessors processors;

    /**
     * Initializes ProcessorTraceProvider with required runtime dependencies and options.
     * @param processors processors.
     */
    public ProcessorTraceProvider(TracingProcessors processors)
    {
        this.processors = processors;
    }

    /**
     * Starts span.
     * @param name human-readable name.
     * @return itrace span result.
     */
    @Override
    public ITraceSpan startSpan(String name)
    {
        return new ITraceSpan()
        {

            /**
 * Span attributes attached to trace events.
             */
            private final Map<String, String> attrs = new HashMap<>();

            /**
             * Adds an attribute to the current span.
             * @param key key.
             * @param value value.
             */
            @Override
            public void annotate(String key, String value)
            {
                attrs.put(key, value);
            }

            /**
             * Closes this value.
             */
            @Override
            public void close()
            {
                processors.emit(new TraceEvent(name, Instant.now(), Map.copyOf(attrs)));
            }
        };
    }
}
