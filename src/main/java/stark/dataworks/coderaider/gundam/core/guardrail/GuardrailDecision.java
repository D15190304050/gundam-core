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
}
