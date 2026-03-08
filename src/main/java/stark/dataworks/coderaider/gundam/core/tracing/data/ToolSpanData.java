package stark.dataworks.coderaider.gundam.core.tracing.data;

import java.util.Map;

/**
 * ToolSpanData implements run tracing and span publication.
 */
public class ToolSpanData extends SpanData
{

    /**
     * Initializes ToolSpanData with required runtime dependencies and options.
     *
     * @param attributes attribute map.
     */
    public ToolSpanData(Map<String, String> attributes)
    {
        super("tool", attributes);
    }
}
