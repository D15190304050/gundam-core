package stark.dataworks.coderaider.gundam.core.output;

import lombok.Getter;

/**
 * OutputValidationResult implements structured output schema validation.
 */
@Getter
public class OutputValidationResult
{

    /**
     * Whether output passed schema validation.
     */
    private final boolean valid;

    /**
     * Reason why execution is allowed or blocked.
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
}
