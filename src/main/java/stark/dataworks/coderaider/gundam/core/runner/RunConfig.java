package stark.dataworks.coderaider.gundam.core.runner;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.policy.RetryPolicy;
import stark.dataworks.coderaider.gundam.core.runerror.RunErrorHandlers;
/**
 * Class RunConfig.
 */

public class RunConfig
{
    /**
     * Field maxTurns.
     */
    private final int maxTurns;
    /**
     * Field sessionId.
     */
    private final String sessionId;
    /**
     * Field temperature.
     */
    private final double temperature;
    /**
     * Field maxOutputTokens.
     */
    private final int maxOutputTokens;
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
     * Field retryPolicy.
     */
    private final RetryPolicy retryPolicy;
    /**
     * Field runErrorHandlers.
     */
    private final RunErrorHandlers runErrorHandlers;
    /**
     * Creates a new RunConfig instance.
     */

    public RunConfig(int maxTurns,
                     String sessionId,
                     double temperature,
                     int maxOutputTokens,
                     String toolChoice,
                     String responseFormat,
                     Map<String, Object> providerOptions)
    {
        this(maxTurns, sessionId, temperature, maxOutputTokens, toolChoice, responseFormat, providerOptions, RetryPolicy.none(), new RunErrorHandlers());
    }
    /**
     * Creates a new RunConfig instance.
     */

    public RunConfig(int maxTurns,
                     String sessionId,
                     double temperature,
                     int maxOutputTokens,
                     String toolChoice,
                     String responseFormat,
                     Map<String, Object> providerOptions,
                     RetryPolicy retryPolicy)
    {
        this(maxTurns, sessionId, temperature, maxOutputTokens, toolChoice, responseFormat, providerOptions, retryPolicy, new RunErrorHandlers());
    }
    /**
     * Creates a new RunConfig instance.
     */

    public RunConfig(int maxTurns,
                     String sessionId,
                     double temperature,
                     int maxOutputTokens,
                     String toolChoice,
                     String responseFormat,
                     Map<String, Object> providerOptions,
                     RetryPolicy retryPolicy,
                     RunErrorHandlers runErrorHandlers)
    {
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
    /**
     * Executes defaults.
     */

    public static RunConfig defaults()
    {
        return new RunConfig(12, null, 0.2, 512, "auto", "text", Map.of());
    }
    /**
     * Executes getMaxTurns.
     */

    public int getMaxTurns()
    {
        return maxTurns;
    }
    /**
     * Executes getSessionId.
     */

    public String getSessionId()
    {
        return sessionId;
    }
    /**
     * Executes getTemperature.
     */

    public double getTemperature()
    {
        return temperature;
    }
    /**
     * Executes getMaxOutputTokens.
     */

    public int getMaxOutputTokens()
    {
        return maxOutputTokens;
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
    /**
     * Executes getRetryPolicy.
     */

    public RetryPolicy getRetryPolicy()
    {
        return retryPolicy;
    }
    /**
     * Executes getRunErrorHandlers.
     */

    public RunErrorHandlers getRunErrorHandlers()
    {
        return runErrorHandlers;
    }
}
