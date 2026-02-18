package stark.dataworks.coderaider.result;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class RunItem {
    private final RunItemType type;
    private final String content;
    private final Map<String, Object> metadata;

    public RunItem(RunItemType type, String content, Map<String, Object> metadata) {
        this.type = Objects.requireNonNull(type, "type");
        this.content = content == null ? "" : content;
        this.metadata = Collections.unmodifiableMap(metadata == null ? Map.of() : metadata);
    }

    public RunItemType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
