package stark.dataworks.coderaider.tracing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.tracing.processor.TracingProcessors;

class TracingProcessorTest {

    @Test
    void processorTraceProviderEmitsEvents() {
        TracingProcessors processors = new TracingProcessors();
        AtomicInteger count = new AtomicInteger();
        processors.add(event -> {
            if ("test.span".equals(event.spanName())) {
                count.incrementAndGet();
            }
        });

        TraceProvider provider = new ProcessorTraceProvider(processors);
        TraceSpan span = provider.startSpan("test.span");
        span.annotate("k", "v");
        span.close();

        assertEquals(1, count.get());
    }
}
