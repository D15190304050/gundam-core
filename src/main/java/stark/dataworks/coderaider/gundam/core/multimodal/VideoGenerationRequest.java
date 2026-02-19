package stark.dataworks.coderaider.gundam.core.multimodal;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * VideoGenerationRequest represents provider-agnostic video generation input.
 */
@Getter
public class VideoGenerationRequest
{
    private final String prompt;
    private final String model;
    private final int durationSeconds;
    private final String aspectRatio;
    private final Map<String, Object> providerOptions;

    public VideoGenerationRequest(String prompt, String model, int durationSeconds, String aspectRatio,
                                  Map<String, Object> providerOptions)
    {
        this.prompt = Objects.requireNonNull(prompt, "prompt");
        this.model = model == null ? "" : model;
        this.durationSeconds = durationSeconds;
        this.aspectRatio = aspectRatio == null ? "" : aspectRatio;
        this.providerOptions = Collections.unmodifiableMap(providerOptions == null ? Map.of() : providerOptions);
    }
}
