package stark.dataworks.coderaider.gundam.core.tracing.data;

import java.util.Map;
/**
 * Class ToolSpanData.
 */

public class ToolSpanData extends SpanData
{
    /**
     * Creates a new ToolSpanData instance.
     */
    public ToolSpanData(Map<String, String> attributes)
    {
        super("tool", attributes);
    }
}
