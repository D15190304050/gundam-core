package stark.dataworks.coderaider.llmspi;

import java.util.Map;

public class LlmOptions {
    private final double temperature;
    private final int maxTokens;
    private final String toolChoice;
    private final String responseFormat;
    private final Map<String, Object> providerOptions;

    public LlmOptions(double temperature, int maxTokens) {
        this(temperature, maxTokens, "auto", "text", Map.of());
    }

    public LlmOptions(double temperature,
                      int maxTokens,
                      String toolChoice,
                      String responseFormat,
                      Map<String, Object> providerOptions) {
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.toolChoice = toolChoice == null ? "auto" : toolChoice;
        this.responseFormat = responseFormat == null ? "text" : responseFormat;
        this.providerOptions = providerOptions == null ? Map.of() : Map.copyOf(providerOptions);
    }

    public double getTemperature() {
        return temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public String getToolChoice() {
        return toolChoice;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public Map<String, Object> getProviderOptions() {
        return providerOptions;
    }
}
