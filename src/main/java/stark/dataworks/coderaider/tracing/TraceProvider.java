package stark.dataworks.coderaider.tracing;

public interface TraceProvider {
    TraceSpan startSpan(String name);
}
