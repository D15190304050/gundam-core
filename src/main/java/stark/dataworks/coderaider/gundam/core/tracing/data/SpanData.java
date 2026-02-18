package stark.dataworks.coderaider.gundam.core.tracing.data;

import java.util.Map;
/**
 * Class SpanData.
 */

public class SpanData
{
    /**
     * Field type.
     */
    private final String type;
    /**
     * Field attributes.
     */
    private final Map<String, String> attributes;
    /**
     * Creates a new SpanData instance.
     */

    public SpanData(String type, Map<String, String> attributes)
    {
        this.type = type;
        this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
    /**
     * Executes getType.
     */

    public String getType()
    {
        return type;
    }
    /**
     * Executes getAttributes.
     */

    public Map<String, String> getAttributes()
    {
        return attributes;
    }
}
