package stark.dataworks.coderaider.gundam.core.multimodal;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * AudioGenerationRequest represents provider-agnostic audio generation input.
 */
@Getter
public class AudioGenerationRequest
{
    private final String prompt;
    private final String model;
    private final String voice;
    private final String format;
    private final Map<String, Object> providerOptions;

    public AudioGenerationRequest(String prompt, String model, String voice, String format, Map<String, Object> providerOptions)
    {
        this.prompt = Objects.requireNonNull(prompt, "prompt");
        this.model = model == null ? "" : model;
        this.voice = voice == null ? "" : voice;
        this.format = format == null ? "" : format;
        this.providerOptions = Collections.unmodifiableMap(providerOptions == null ? Map.of() : providerOptions);
    }
}
