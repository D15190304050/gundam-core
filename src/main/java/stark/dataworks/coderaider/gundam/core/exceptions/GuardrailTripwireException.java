package stark.dataworks.coderaider.gundam.core.exceptions;

/**
 * GuardrailTripwireException implements core runtime responsibilities.
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
