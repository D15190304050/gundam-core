package stark.dataworks.coderaider.gundam.core.result;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
/**
 * Class RunItem.
 */

public class RunItem
{
    /**
     * Field type.
     */
    private final RunItemType type;
    /**
     * Field content.
     */
    private final String content;
    /**
     * Field metadata.
     */
    private final Map<String, Object> metadata;
    /**
     * Creates a new RunItem instance.
     */

    public RunItem(RunItemType type, String content, Map<String, Object> metadata)
    {
        this.type = Objects.requireNonNull(type, "type");
        this.content = content == null ? "" : content;
        this.metadata = Collections.unmodifiableMap(metadata == null ? Map.of() : metadata);
    }
    /**
     * Executes getType.
     */

    public RunItemType getType()
    {
        return type;
    }
    /**
     * Executes getContent.
     */

    public String getContent()
    {
        return content;
    }
    /**
     * Executes getMetadata.
     */

    public Map<String, Object> getMetadata()
    {
        return metadata;
    }
}
