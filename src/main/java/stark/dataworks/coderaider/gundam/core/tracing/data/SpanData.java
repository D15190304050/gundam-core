package stark.dataworks.coderaider.gundam.core.tracing.data;

import lombok.Getter;

import java.util.Map;

/**
 * SpanData implements run tracing and span publication.
 */
@Getter
public class SpanData
{

    /**
     * Type discriminator for this item/event/span.
     */
    private final String type;

    /**
     * Additional key-value payload fields.
     */
    private final Map<String, String> attributes;

    /**
     * Initializes SpanData with required runtime dependencies and options.
     * @param type type discriminator.
     * @param Map<String map<string.
     * @param attributes attribute map.
     */
    public SpanData(String type, Map<String, String> attributes)
    {
        this.type = type;
        this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
