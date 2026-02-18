package stark.dataworks.coderaider.gundam.core.tracing.data;

import java.util.Map;

/**
 * SpanData implements run tracing and span publication.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class SpanData
{

    /**
     * Internal state for type; used while coordinating runtime behavior.
     */
    private final String type;

    /**
     * Internal state for attributes; used while coordinating runtime behavior.
     */
    private final Map<String, String> attributes;

    /**
     * Performs span data as part of SpanData runtime responsibilities.
     * @param type The type used by this operation.
     * @param attributes The attributes used by this operation.
     */
    public SpanData(String type, Map<String, String> attributes)
    {
        this.type = type;
        this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    /**
     * Returns the current type value maintained by this SpanData.
     * @return The value produced by this operation.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Returns the current attributes value maintained by this SpanData.
     * @return The value produced by this operation.
     */
    public Map<String, String> getAttributes()
    {
        return attributes;
    }
}
