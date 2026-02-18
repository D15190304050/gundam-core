package stark.dataworks.coderaider.gundam.core.tracing;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.tracing.processor.TracingProcessors;

/**
 * ProcessorTraceProvider implements run tracing and span publication.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class ProcessorTraceProvider implements TraceProvider
{

    /**
     * Internal state for processors; used while coordinating runtime behavior.
     */
    private final TracingProcessors processors;

    /**
     * Performs processor trace provider as part of ProcessorTraceProvider runtime responsibilities.
     * @param processors The processors used by this operation.
     */
    public ProcessorTraceProvider(TracingProcessors processors)
    {
        this.processors = processors;
    }

    /**
     * Performs start span as part of ProcessorTraceProvider runtime responsibilities.
     * @param name The name used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public TraceSpan startSpan(String name)
    {
        return new TraceSpan()
        {

            /**
             * Internal state for attrs used while coordinating runtime behavior.
             */
            private final Map<String, String> attrs = new HashMap<>();

            /**
             * Performs annotate as part of ProcessorTraceProvider runtime responsibilities.
             * @param key The key used by this operation.
             * @param value The value used by this operation.
             */
            @Override
            public void annotate(String key, String value)
            {
                attrs.put(key, value);
            }

            /**
             * Performs close as part of ProcessorTraceProvider runtime responsibilities.
             */
            @Override
            public void close()
            {
                processors.emit(new TraceEvent(name, Instant.now(), Map.copyOf(attrs)));
            }
        };
    }
}
