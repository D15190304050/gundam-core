package stark.dataworks.coderaider.gundam.core.output;

/**
 * OutputValidationResult implements structured output schema validation.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class OutputValidationResult
{

    /**
     * Internal state for valid; used while coordinating runtime behavior.
     */
    private final boolean valid;

    /**
     * Internal state for reason; used while coordinating runtime behavior.
     */
    private final String reason;

    /**
     * Performs output validation result as part of OutputValidationResult runtime responsibilities.
     * @param valid The valid used by this operation.
     * @param reason The reason used by this operation.
     */
    private OutputValidationResult(boolean valid, String reason)
    {
        this.valid = valid;
        this.reason = reason == null ? "" : reason;
    }

    /**
     * Performs ok as part of OutputValidationResult runtime responsibilities.
     * @return The value produced by this operation.
     */
    public static OutputValidationResult ok()
    {
        return new OutputValidationResult(true, "");
    }

    /**
     * Performs fail as part of OutputValidationResult runtime responsibilities.
     * @param reason The reason used by this operation.
     * @return The value produced by this operation.
     */
    public static OutputValidationResult fail(String reason)
    {
        return new OutputValidationResult(false, reason);
    }

    /**
     * Reports whether valid is currently satisfied.
     * @return {@code true} when the condition is satisfied; otherwise {@code false}.
     */
    public boolean isValid()
    {
        return valid;
    }

    /**
     * Returns the current reason value maintained by this OutputValidationResult.
     * @return The value produced by this operation.
     */
    public String getReason()
    {
        return reason;
    }
}
