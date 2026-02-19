package stark.dataworks.coderaider.gundam.core.realtime;

import lombok.Getter;

/**
 * Configuration for realtime bidirectional sessions.
 */
@Getter
public class RealtimeSessionConfig
{
    private final String model;
    private final String voice;
    private final int maxOutputTokens;

    public RealtimeSessionConfig(String model, String voice, int maxOutputTokens)
    {
        if (model == null || model.isBlank())
        {
            throw new IllegalArgumentException("model is required");
        }
        if (maxOutputTokens < 1)
        {
            throw new IllegalArgumentException("maxOutputTokens must be >= 1");
        }
        this.model = model;
        this.voice = voice == null ? "alloy" : voice;
        this.maxOutputTokens = maxOutputTokens;
    }
}
