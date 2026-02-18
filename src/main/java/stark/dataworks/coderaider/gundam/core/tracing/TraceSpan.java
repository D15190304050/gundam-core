package stark.dataworks.coderaider.gundam.core.tracing;

public interface TraceSpan {
    void annotate(String key, String value);

    void close();
}
