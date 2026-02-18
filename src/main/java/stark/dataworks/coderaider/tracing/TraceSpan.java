package stark.dataworks.coderaider.tracing;

public interface TraceSpan {
    void annotate(String key, String value);

    void close();
}
