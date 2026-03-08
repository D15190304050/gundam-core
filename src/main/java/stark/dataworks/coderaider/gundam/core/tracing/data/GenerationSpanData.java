package stark.dataworks.coderaider.gundam.core.tracing.data;

import java.util.Map;

/**
 * GenerationSpanData implements run tracing and span publication.
 */
public class GenerationSpanData extends SpanData
{

    /**
     * Initializes GenerationSpanData with required runtime dependencies and options.
     *
     * @param attributes attribute map.
     */
    public GenerationSpanData(Map<String, String> attributes)
    {
        super("generation", attributes);
    }
}
