package stark.dataworks.coderaider.gundam.core.tracing.processor;

import stark.dataworks.coderaider.gundam.core.tracing.TraceEvent;
/**
 * Interface TracingProcessor.
 */

public interface TracingProcessor
{
    /**
     * Executes process.
     */
    void process(TraceEvent event);
}
