package stark.dataworks.coderaider.gundam.core.llmspi;

import lombok.Getter;

import java.util.Map;

/**
 * LlmOptions implements provider-agnostic model invocation contracts.
 */
@Getter
public class LlmOptions
{

    /**
     * Sampling temperature for this request.
     */
    private final double temperature;

    /**
     * Maximum completion tokens allowed for this invocation.
     */
    private final int maxTokens;

    /**
     * Tool choice policy used for this model invocation.
     */
    private final String toolChoice;

    /**
     * Response format requested from the model provider.
     */
    private final String responseFormat;

    /**
     * Provider-specific options attached to this model call.
     */
    private final Map<String, Object> providerOptions;

    /**
     * Initializes LlmOptions with required runtime dependencies and options.
     * @param temperature sampling temperature.
     * @param maxTokens maximum token limit.
     */
    public LlmOptions(double temperature, int maxTokens)
    {
        this(temperature, maxTokens, "auto", "text", Map.of());
    }

    /**
     * Creates LLM options.
     * @param temperature sampling temperature.
     * @param maxTokens maximum token limit.
     * @param toolChoice tool-choice policy passed to the model provider.
     * @param responseFormat response format requested from the model provider.
     * @param providerOptions provider-specific model options.
     */
    public LlmOptions(double temperature,
                      int maxTokens,
                      String toolChoice,
                      String responseFormat,
                      Map<String, Object> providerOptions)
    {
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.toolChoice = toolChoice == null ? "auto" : toolChoice;
        this.responseFormat = responseFormat == null ? "text" : responseFormat;
        this.providerOptions = providerOptions == null ? Map.of() : Map.copyOf(providerOptions);
    }
}
