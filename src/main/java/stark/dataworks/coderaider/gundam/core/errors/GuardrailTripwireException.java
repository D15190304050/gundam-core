package stark.dataworks.coderaider.gundam.core.errors;

/**
 * GuardrailTripwireException implements core runtime responsibilities.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class GuardrailTripwireException extends AgentsException
{

    /**
     * Performs guardrail tripwire exception as part of GuardrailTripwireException runtime responsibilities.
     * @param phase The phase used by this operation.
     * @param reason The reason used by this operation.
     */
    public GuardrailTripwireException(String phase, String reason)
    {
        super("Guardrail triggered at " + phase + ": " + reason);
    }
}
