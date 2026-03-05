package stark.dataworks.coderaider.gundam.core.context;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * RunItem implements normalized run result structures.
 */
@Getter
public class ContextItem
{

    /**
     * Type discriminator for this item/event/span.
     */
    private final ContextItemType type;

    /**
     * Main assistant text content returned by the model.
     */
    private final String content;

    /**
     * Arbitrary metadata attached for caller-specific routing or auditing.
     */
    private final Map<String, Object> metadata;

    /**
     * Initializes ContextItem with required runtime dependencies and options.
     * @param type type discriminator.
     * @param content content.
     * @param Map<String map<string.
     * @param metadata metadata map.
     */
    public ContextItem(ContextItemType type, String content, Map<String, Object> metadata)
    {
        this.type = Objects.requireNonNull(type, "type");
        this.content = content == null ? "" : content;
        this.metadata = Collections.unmodifiableMap(metadata == null ? Map.of() : metadata);
    }
}
