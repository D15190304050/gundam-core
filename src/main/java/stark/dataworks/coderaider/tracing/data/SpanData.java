package stark.dataworks.coderaider.tracing.data;

import java.util.Map;

public class SpanData {
    private final String type;
    private final Map<String, String> attributes;

    public SpanData(String type, Map<String, String> attributes) {
        this.type = type;
        this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
