package stark.dataworks.coderaider.gundam.core.result;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * RunItem implements normalized run result structures.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class RunItem
{

    /**
     * Internal state for type; used while coordinating runtime behavior.
     */
    private final RunItemType type;

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
    public RunItem(RunItemType type, String content, Map<String, Object> metadata)
    {
        this.type = Objects.requireNonNull(type, "type");
        this.content = content == null ? "" : content;
        this.metadata = Collections.unmodifiableMap(metadata == null ? Map.of() : metadata);
    }

    /**
     * Returns the current type value maintained by this RunItem.
     * @return The value produced by this operation.
     */
    public RunItemType getType()
    {
        return type;
    }

    /**
     * Returns the current content value maintained by this RunItem.
     * @return The value produced by this operation.
     */
    public String getContent()
    {
        return content;
    }

    /**
     * Returns the current metadata value maintained by this RunItem.
     * @return The value produced by this operation.
     */
    public Map<String, Object> getMetadata()
    {
        return metadata;
    }
}
