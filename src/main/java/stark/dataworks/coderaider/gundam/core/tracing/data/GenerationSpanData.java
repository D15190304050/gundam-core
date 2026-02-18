package stark.dataworks.coderaider.gundam.core.tracing.data;

import java.util.Map;

public class GenerationSpanData extends SpanData {
    public GenerationSpanData(Map<String, String> attributes) {
        super("generation", attributes);
    }
}
