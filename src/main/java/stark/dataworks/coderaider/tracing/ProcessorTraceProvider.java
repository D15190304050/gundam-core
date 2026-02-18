package stark.dataworks.coderaider.tracing;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import stark.dataworks.coderaider.tracing.processor.TracingProcessors;

public class ProcessorTraceProvider implements TraceProvider {
    private final TracingProcessors processors;

    public ProcessorTraceProvider(TracingProcessors processors) {
        this.processors = processors;
    }

    @Override
    public TraceSpan startSpan(String name) {
        return new TraceSpan() {
            private final Map<String, String> attrs = new HashMap<>();

            @Override
            public void annotate(String key, String value) {
                attrs.put(key, value);
            }

            @Override
            public void close() {
                processors.emit(new TraceEvent(name, Instant.now(), Map.copyOf(attrs)));
            }
        };
    }
}
