package stark.dataworks.coderaider.gundam.core.tracing.data;

import java.util.Map;

/**
 * ToolSpanData implements run tracing and span publication.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class ToolSpanData extends SpanData
{

    /**
     * Performs tool span data as part of ToolSpanData runtime responsibilities.
     * @param attributes The attributes used by this operation.
     */
    public ToolSpanData(Map<String, String> attributes)
    {
        super("tool", attributes);
    }
}
