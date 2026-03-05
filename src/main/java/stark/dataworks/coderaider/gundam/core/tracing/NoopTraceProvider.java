package stark.dataworks.coderaider.gundam.core.tracing;

/**
 * NoopTraceProvider implements run tracing and span publication.
 */
public class NoopTraceProvider implements ITraceProvider
{

    /**
     * Starts span.
     * @param name human-readable name.
     * @return itrace span result.
     */
    @Override
    public ITraceSpan startSpan(String name)
    {
        return new ITraceSpan()
        {

            /**
             * Adds an attribute to the current span.
             * @param key key.
             * @param value value.
             */
            @Override
            public void annotate(String key, String value)
            {
            }

            /**
             * Closes this value.
             */
            @Override
            public void close()
            {
            }
        };
    }
}
