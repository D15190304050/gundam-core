package stark.dataworks.coderaider.gundam.core.tracing.processor;

import stark.dataworks.coderaider.gundam.core.tracing.TraceEvent;

public interface TracingProcessor {
    void process(TraceEvent event);
}
