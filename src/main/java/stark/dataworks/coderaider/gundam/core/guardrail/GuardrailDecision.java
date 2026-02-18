package stark.dataworks.coderaider.gundam.core.guardrail;

import java.util.Objects;
/**
 * Class GuardrailDecision.
 */

public class GuardrailDecision
{
    /**
     * Field allowed.
     */
    private final boolean allowed;
    /**
     * Field reason.
     */
    private final String reason;
    /**
     * Creates a new GuardrailDecision instance.
     */

    private GuardrailDecision(boolean allowed, String reason)
    {
        this.allowed = allowed;
        this.reason = reason == null ? "" : reason;
    }
    /**
     * Executes allow.
     */

    public static GuardrailDecision allow()
    {
        return new GuardrailDecision(true, "");
    }
    /**
     * Executes deny.
     */

    public static GuardrailDecision deny(String reason)
    {
        return new GuardrailDecision(false, Objects.requireNonNull(reason, "reason"));
    }
    /**
     * Executes isAllowed.
     */

    public boolean isAllowed()
    {
        return allowed;
    }
    /**
     * Executes getReason.
     */

    public String getReason()
    {
        return reason;
    }
}
