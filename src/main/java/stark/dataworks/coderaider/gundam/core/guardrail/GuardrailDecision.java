package stark.dataworks.coderaider.gundam.core.guardrail;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * GuardrailDecision implements input/output policy evaluation around model responses.
 */
@Getter
@AllArgsConstructor
public class GuardrailDecision
{

    /**
     * Whether processing is allowed to continue after a guardrail check.
     */
    private final boolean allowed;

    /**
     * Reason why execution is allowed or blocked.
     */
    private final String reason;

    /**
     * Returns an allow decision.
     * @return guardrail decision result.
     */
    public static GuardrailDecision allow()
    {
        return new GuardrailDecision(true, "");
    }

    /**
     * Returns a deny decision with a reason.
     * @param reason human-readable reason.
     * @return guardrail decision result.
     */
    public static GuardrailDecision deny(String reason)
    {
        return new GuardrailDecision(false, Objects.requireNonNull(reason, "reason"));
    }
}
