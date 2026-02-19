package stark.dataworks.coderaider.gundam.core.tracing.processor;

import stark.dataworks.coderaider.gundam.core.tracing.TraceEvent;

/**
 * TracingProcessor implements run tracing and span publication.
 */
public interface ITracingProcessor
{

    /**
     * Performs process as part of TracingProcessor runtime responsibilities.
     * @param event The event used by this operation.
     */
    void process(TraceEvent event);
}
