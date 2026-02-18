package stark.dataworks.coderaider.gundam.core.llmspi;

import lombok.Getter;

import java.util.Map;

/**
 * LlmOptions implements provider-agnostic model invocation contracts.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
@Getter
public class LlmOptions
{

    /**
     * Internal state for temperature; used while coordinating runtime behavior.
     */
    private final double temperature;

    /**
     * Internal state for max tokens; used while coordinating runtime behavior.
     */
    private final int maxTokens;

    /**
     * Internal state for tool choice; used while coordinating runtime behavior.
     */
    private final String toolChoice;

    /**
     * Internal state for response format; used while coordinating runtime behavior.
     */
    private final String responseFormat;

    /**
     * Internal state for provider options; used while coordinating runtime behavior.
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
