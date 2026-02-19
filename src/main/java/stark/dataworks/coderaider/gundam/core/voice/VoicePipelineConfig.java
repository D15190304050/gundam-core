package stark.dataworks.coderaider.gundam.core.voice;

import lombok.Getter;

/**
 * Configuration for an end-to-end voice agent pipeline.
 */
@Getter
public class VoicePipelineConfig
{
    private final String model;
    private final String voice;
    private final String instructions;

    public VoicePipelineConfig(String model, String voice, String instructions)
    {
        if (model == null || model.isBlank())
        {
            throw new IllegalArgumentException("model is required");
        }
        this.model = model;
        this.voice = voice == null || voice.isBlank() ? "alloy" : voice;
        this.instructions = instructions == null ? "" : instructions;
    }
}
