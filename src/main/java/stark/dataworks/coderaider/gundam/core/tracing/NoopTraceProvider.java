package stark.dataworks.coderaider.gundam.core.tracing;
/**
 * Class NoopTraceProvider.
 */

public class NoopTraceProvider implements TraceProvider
{
    /**
     * Executes startSpan.
     */
    @Override
    public TraceSpan startSpan(String name)
    {
        return new TraceSpan()
        {
            /**
             * Executes annotate.
             */
            @Override
            public void annotate(String key, String value)
            {
            }

            /**
             * Executes close.
             */
            @Override
            public void close()
            {
            }
        };
    }
}
