package stark.dataworks.coderaider.gundam.core.llmspi;

import java.util.Map;
/**
 * Class LlmOptions.
 */

public class LlmOptions
{
    /**
     * Field temperature.
     */
    private final double temperature;
    /**
     * Field maxTokens.
     */
    private final int maxTokens;
    /**
     * Field toolChoice.
     */
    private final String toolChoice;
    /**
     * Field responseFormat.
     */
    private final String responseFormat;
    /**
     * Field providerOptions.
     */
    private final Map<String, Object> providerOptions;
    /**
     * Creates a new LlmOptions instance.
     */

    public LlmOptions(double temperature, int maxTokens)
    {
        this(temperature, maxTokens, "auto", "text", Map.of());
    }
    /**
     * Creates a new LlmOptions instance.
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
    /**
     * Executes getTemperature.
     */

    public double getTemperature()
    {
        return temperature;
    }
    /**
     * Executes getMaxTokens.
     */

    public int getMaxTokens()
    {
        return maxTokens;
    }
    /**
     * Executes getToolChoice.
     */

    public String getToolChoice()
    {
        return toolChoice;
    }
    /**
     * Executes getResponseFormat.
     */

    public String getResponseFormat()
    {
        return responseFormat;
    }
    /**
     * Executes getProviderOptions.
     */

    public Map<String, Object> getProviderOptions()
    {
        return providerOptions;
    }
}
