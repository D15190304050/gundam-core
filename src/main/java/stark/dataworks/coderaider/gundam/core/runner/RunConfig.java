package stark.dataworks.coderaider.gundam.core.runner;

import lombok.Getter;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.policy.RetryPolicy;
import stark.dataworks.coderaider.gundam.core.runerror.RunErrorHandlers;

/**
 * Immutable runtime configuration used by {@link AdvancedAgentRunner}.
 * <p>
 * The configuration centralizes turn limits, generation settings, retry behavior, and error-handler strategy so each
 * run can be controlled without changing code.
 */
@Getter
public class RunConfig
{

    /**
     * Hard upper bound for run turns before the runner raises max-turns exceeded.
     */
    private final int maxTurns;

    /**
     * Optional session id used to restore/persist memory across run invocations.
     */
    private final String sessionId;

    /**
     * Sampling temperature forwarded to the model provider.
     */
    private final double temperature;

    /**
     * Maximum output token budget requested from the model provider.
     */
    private final int maxOutputTokens;

    /**
     * Tool-choice strategy (for example: {@code auto}, {@code none}, or provider-specific value).
     */
    private final String toolChoice;

    /**
     * Requested response format (for example plain text or structured output).
     */
    private final String responseFormat;

    /**
     * Provider-specific generation options passed through transparently to the model adapter.
     */
    private final Map<String, Object> providerOptions;

    /**
     * Retry/backoff policy used when model invocation fails transiently.
     */
    private final RetryPolicy retryPolicy;

    /**
     * Error-handler dispatch table used to recover or finalize on runtime failures.
     */
    private final RunErrorHandlers runErrorHandlers;

    /**
     * Performs run config as part of RunConfig runtime responsibilities.
     * @param maxTurns The max turns used by this operation.
     * @param sessionId The session id used by this operation.
     * @param temperature The temperature used by this operation.
     * @param maxOutputTokens The max output tokens used by this operation.
     * @param toolChoice The tool choice used by this operation.
     * @param responseFormat The response format used by this operation.
     * @param providerOptions The provider options used by this operation.
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
     * Creates a run configuration with explicit retry policy and default error handlers.
     * @param maxTurns Maximum number of turns before stopping.
     * @param sessionId Optional session id for conversation resume.
     * @param temperature Model temperature.
     * @param maxOutputTokens Maximum output token budget.
     * @param toolChoice Tool-choice behavior to pass to the provider.
     * @param responseFormat Response format requested from the provider.
     * @param providerOptions Provider-specific options.
     * @param retryPolicy Retry/backoff policy.
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
     * Builds a practical default configuration for local development and tests.
     * @return Default run configuration.
     */
    public static RunConfig defaults()
    {
        return new RunConfig(12, null, 0.2, 512, "auto", "text", Map.of());
    }
}
