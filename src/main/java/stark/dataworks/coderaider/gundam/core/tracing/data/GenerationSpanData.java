package stark.dataworks.coderaider.gundam.core.tracing.data;

import java.util.Map;
/**
 * Class GenerationSpanData.
 */

public class GenerationSpanData extends SpanData
{
    /**
     * Creates a new GenerationSpanData instance.
     */
    public GenerationSpanData(Map<String, String> attributes)
    {
        super("generation", attributes);
    }
}
