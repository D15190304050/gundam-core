package stark.dataworks.coderaider.tracing.processor;

import stark.dataworks.coderaider.tracing.TraceEvent;

public interface TracingProcessor {
    void process(TraceEvent event);
}
