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
     * Internal state for type; used while coordinating runtime behavior.
     */
    private final ContextItemType type;

    /**
     * Internal state for content; used while coordinating runtime behavior.
     */
    private final String content;

    /**
     * Internal state for metadata; used while coordinating runtime behavior.
     */
    private final Map<String, Object> metadata;

    /**
     * Performs run item as part of RunItem runtime responsibilities.
     * @param type The type used by this operation.
     * @param content The content used by this operation.
     * @param metadata The metadata used by this operation.
     */
    public ContextItem(ContextItemType type, String content, Map<String, Object> metadata)
    {
        this.type = Objects.requireNonNull(type, "type");
        this.content = content == null ? "" : content;
        this.metadata = Collections.unmodifiableMap(metadata == null ? Map.of() : metadata);
    }
}
