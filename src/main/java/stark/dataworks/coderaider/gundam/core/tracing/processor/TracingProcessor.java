package stark.dataworks.coderaider.gundam.core.tracing.processor;

import stark.dataworks.coderaider.gundam.core.tracing.TraceEvent;

/**
 * TracingProcessor implements run tracing and span publication.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface TracingProcessor
{

    /**
     * Performs process as part of TracingProcessor runtime responsibilities.
     * @param event The event used by this operation.
     */
    void process(TraceEvent event);
}
