package stark.dataworks.coderaider.gundam.core.tracing;

/**
 * NoopTraceProvider implements run tracing and span publication.
 */
public class NoopTraceProvider implements ITraceProvider
{

    /**
     * Performs start span as part of NoopTraceProvider runtime responsibilities.
     * @param name The name used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public ITraceSpan startSpan(String name)
    {
        return new ITraceSpan()
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
