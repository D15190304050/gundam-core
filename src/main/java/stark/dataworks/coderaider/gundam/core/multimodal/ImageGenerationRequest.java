package stark.dataworks.coderaider.gundam.core.multimodal;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * ImageGenerationRequest represents provider-agnostic image generation input.
 */
@Getter
public class ImageGenerationRequest
{
    private final String prompt;
    private final String model;
    private final String size;
    private final Map<String, Object> providerOptions;

    public ImageGenerationRequest(String prompt, String model, String size, Map<String, Object> providerOptions)
    {
        this.prompt = Objects.requireNonNull(prompt, "prompt");
        this.model = model == null ? "" : model;
        this.size = size == null ? "" : size;
        this.providerOptions = Collections.unmodifiableMap(providerOptions == null ? Map.of() : providerOptions);
    }
}
