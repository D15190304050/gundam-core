package stark.dataworks.coderaider.gundam.core.tracing;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.tracing.processor.TracingProcessors;
/**
 * Class ProcessorTraceProvider.
 */

public class ProcessorTraceProvider implements TraceProvider
{
    /**
     * Field processors.
     */
    private final TracingProcessors processors;
    /**
     * Creates a new ProcessorTraceProvider instance.
     */

    public ProcessorTraceProvider(TracingProcessors processors)
    {
        this.processors = processors;
    }

    /**
     * Executes startSpan.
     */
    @Override
    public TraceSpan startSpan(String name)
    {
        return new TraceSpan()
        {
            /**
             * Field attrs.
             */
            private final Map<String, String> attrs = new HashMap<>();

            /**
             * Executes annotate.
             */
            @Override
            public void annotate(String key, String value)
            {
                attrs.put(key, value);
            }

            /**
             * Executes close.
             */
            @Override
            public void close()
            {
                processors.emit(new TraceEvent(name, Instant.now(), Map.copyOf(attrs)));
            }
        };
    }
}
