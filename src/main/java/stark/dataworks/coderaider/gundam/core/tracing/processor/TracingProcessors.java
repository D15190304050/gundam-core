package stark.dataworks.coderaider.gundam.core.tracing.processor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import stark.dataworks.coderaider.gundam.core.tracing.TraceEvent;

public class TracingProcessors {
    private final List<TracingProcessor> processors = new CopyOnWriteArrayList<>();

    public void add(TracingProcessor processor) {
        processors.add(processor);
    }

    public void emit(TraceEvent event) {
        processors.forEach(p -> p.process(event));
    }
}
