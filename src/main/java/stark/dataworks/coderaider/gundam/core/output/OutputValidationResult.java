package stark.dataworks.coderaider.gundam.core.output;
/**
 * Class OutputValidationResult.
 */

public class OutputValidationResult
{
    /**
     * Field valid.
     */
    private final boolean valid;
    /**
     * Field reason.
     */
    private final String reason;
    /**
     * Creates a new OutputValidationResult instance.
     */

    private OutputValidationResult(boolean valid, String reason)
    {
        this.valid = valid;
        this.reason = reason == null ? "" : reason;
    }
    /**
     * Executes ok.
     */

    public static OutputValidationResult ok()
    {
        return new OutputValidationResult(true, "");
    }
    /**
     * Executes fail.
     */

    public static OutputValidationResult fail(String reason)
    {
        return new OutputValidationResult(false, reason);
    }
    /**
     * Executes isValid.
     */

    public boolean isValid()
    {
        return valid;
    }
    /**
     * Executes getReason.
     */

    public String getReason()
    {
        return reason;
    }
}
