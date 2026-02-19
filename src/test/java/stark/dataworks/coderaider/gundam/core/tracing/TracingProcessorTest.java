package stark.dataworks.coderaider.gundam.core.tracing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.tracing.processor.TracingProcessors;

class TracingProcessorTest
{

    @Test
    void processorTraceProviderEmitsEvents()
    {
        TracingProcessors processors = new TracingProcessors();
        AtomicInteger count = new AtomicInteger();
        processors.add(event ->
        {
            if ("test.span".equals(event.spanName()))
            {
                count.incrementAndGet();
            }
        });

        ITraceProvider provider = new ProcessorTraceProvider(processors);
        ITraceSpan span = provider.startSpan("test.span");
        span.annotate("k", "v");
        span.close();

        assertEquals(1, count.get());
    }
}
