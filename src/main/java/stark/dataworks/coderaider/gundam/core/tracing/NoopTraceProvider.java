package stark.dataworks.coderaider.gundam.core.tracing;

/**
 * NoopTraceProvider implements run tracing and span publication.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class NoopTraceProvider implements TraceProvider
{

    /**
     * Performs start span as part of NoopTraceProvider runtime responsibilities.
     * @param name The name used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public TraceSpan startSpan(String name)
    {
        return new TraceSpan()
        {

            /**
             * Performs annotate as part of NoopTraceProvider runtime responsibilities.
             * @param key The key used by this operation.
             * @param value The value used by this operation.
             */
            @Override
            public void annotate(String key, String value)
            {
            }

            /**
             * Performs close as part of NoopTraceProvider runtime responsibilities.
             */
            @Override
            public void close()
            {
            }
        };
    }
}
