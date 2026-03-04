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
     * Performs llm options as part of LlmOptions runtime responsibilities.
     * @param temperature The temperature used by this operation.
     * @param maxTokens The max tokens used by this operation.
     */
    public LlmOptions(double temperature, int maxTokens)
    {
        this(temperature, maxTokens, "auto", "text", Map.of());
    }

    /**
     * Performs llm options as part of LlmOptions runtime responsibilities.
     * @param temperature The temperature used by this operation.
     * @param maxTokens The max tokens used by this operation.
     * @param toolChoice The tool choice used by this operation.
     * @param responseFormat The response format used by this operation.
     * @param providerOptions The provider options used by this operation.
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
