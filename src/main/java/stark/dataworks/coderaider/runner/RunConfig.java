package stark.dataworks.coderaider.runner;

import java.util.Map;
import stark.dataworks.coderaider.policy.RetryPolicy;
import stark.dataworks.coderaider.runerror.RunErrorHandlers;

public class RunConfig {
    private final int maxTurns;
    private final String sessionId;
    private final double temperature;
    private final int maxOutputTokens;
    private final String toolChoice;
    private final String responseFormat;
    private final Map<String, Object> providerOptions;
    private final RetryPolicy retryPolicy;
    private final RunErrorHandlers runErrorHandlers;

    public RunConfig(int maxTurns,
                     String sessionId,
                     double temperature,
                     int maxOutputTokens,
                     String toolChoice,
                     String responseFormat,
                     Map<String, Object> providerOptions) {
        this(maxTurns, sessionId, temperature, maxOutputTokens, toolChoice, responseFormat, providerOptions, RetryPolicy.none(), new RunErrorHandlers());
    }

    public RunConfig(int maxTurns,
                     String sessionId,
                     double temperature,
                     int maxOutputTokens,
                     String toolChoice,
                     String responseFormat,
                     Map<String, Object> providerOptions,
                     RetryPolicy retryPolicy) {
        this(maxTurns, sessionId, temperature, maxOutputTokens, toolChoice, responseFormat, providerOptions, retryPolicy, new RunErrorHandlers());
    }

    public RunConfig(int maxTurns,
                     String sessionId,
                     double temperature,
                     int maxOutputTokens,
                     String toolChoice,
                     String responseFormat,
                     Map<String, Object> providerOptions,
                     RetryPolicy retryPolicy,
                     RunErrorHandlers runErrorHandlers) {
        this.maxTurns = maxTurns;
        this.sessionId = sessionId;
        this.temperature = temperature;
        this.maxOutputTokens = maxOutputTokens;
        this.toolChoice = toolChoice == null ? "auto" : toolChoice;
        this.responseFormat = responseFormat == null ? "text" : responseFormat;
        this.providerOptions = providerOptions == null ? Map.of() : Map.copyOf(providerOptions);
        this.retryPolicy = retryPolicy == null ? RetryPolicy.none() : retryPolicy;
        this.runErrorHandlers = runErrorHandlers == null ? new RunErrorHandlers() : runErrorHandlers;
    }

    public static RunConfig defaults() {
        return new RunConfig(12, null, 0.2, 512, "auto", "text", Map.of());
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public String getSessionId() {
        return sessionId;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getMaxOutputTokens() {
        return maxOutputTokens;
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

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public RunErrorHandlers getRunErrorHandlers() {
        return runErrorHandlers;
    }
}
