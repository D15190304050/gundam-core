package stark.dataworks.coderaider.tracing;

public class NoopTraceProvider implements TraceProvider {
    @Override
    public TraceSpan startSpan(String name) {
        return new TraceSpan() {
            @Override
            public void annotate(String key, String value) {
            }

            @Override
            public void close() {
            }
        };
    }
}
