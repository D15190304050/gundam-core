package stark.dataworks.coderaider.tracing.data;

import java.util.Map;

public class GenerationSpanData extends SpanData {
    public GenerationSpanData(Map<String, String> attributes) {
        super("generation", attributes);
    }
}
