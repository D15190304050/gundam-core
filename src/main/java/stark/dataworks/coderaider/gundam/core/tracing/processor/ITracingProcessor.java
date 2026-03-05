package stark.dataworks.coderaider.gundam.core.tracing.processor;

import stark.dataworks.coderaider.gundam.core.tracing.TraceEvent;

/**
 * TracingProcessor implements run tracing and span publication.
 */
public interface ITracingProcessor
{

    /**
     * Processes the supplied workflow/tracing input.
     * @param event run event.
     */
    void process(TraceEvent event);
}
