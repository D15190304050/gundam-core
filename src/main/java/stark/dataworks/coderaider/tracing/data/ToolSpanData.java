package stark.dataworks.coderaider.tracing.data;

import java.util.Map;

public class ToolSpanData extends SpanData {
    public ToolSpanData(Map<String, String> attributes) {
        super("tool", attributes);
    }
}
