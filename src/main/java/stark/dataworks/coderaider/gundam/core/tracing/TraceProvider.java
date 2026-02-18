package stark.dataworks.coderaider.gundam.core.tracing;

public interface TraceProvider {
    TraceSpan startSpan(String name);
}
