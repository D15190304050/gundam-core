package stark.dataworks.coderaider.gundam.core.guardrail;

import java.util.Objects;

/**
 * GuardrailDecision implements input/output policy evaluation around model responses.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class GuardrailDecision
{

    /**
     * Internal state for allowed; used while coordinating runtime behavior.
     */
    private final boolean allowed;

    /**
     * Internal state for reason; used while coordinating runtime behavior.
     */
    private final String reason;

    /**
     * Performs guardrail decision as part of GuardrailDecision runtime responsibilities.
     * @param allowed The allowed used by this operation.
     * @param reason The reason used by this operation.
     */
    private GuardrailDecision(boolean allowed, String reason)
    {
        this.allowed = allowed;
        this.reason = reason == null ? "" : reason;
    }

    /**
     * Performs allow as part of GuardrailDecision runtime responsibilities.
     * @return The value produced by this operation.
     */
    public static GuardrailDecision allow()
    {
        return new GuardrailDecision(true, "");
    }

    /**
     * Performs deny as part of GuardrailDecision runtime responsibilities.
     * @param reason The reason used by this operation.
     * @return The value produced by this operation.
     */
    public static GuardrailDecision deny(String reason)
    {
        return new GuardrailDecision(false, Objects.requireNonNull(reason, "reason"));
    }

    /**
     * Reports whether allowed is currently satisfied.
     * @return {@code true} when the condition is satisfied; otherwise {@code false}.
     */
    public boolean isAllowed()
    {
        return allowed;
    }

    /**
     * Returns the current reason value maintained by this GuardrailDecision.
     * @return The value produced by this operation.
     */
    public String getReason()
    {
        return reason;
    }
}
