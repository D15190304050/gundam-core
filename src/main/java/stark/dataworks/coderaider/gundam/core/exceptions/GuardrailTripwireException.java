package stark.dataworks.coderaider.gundam.core.exceptions;

/**
 * GuardrailTripwireException implements core runtime responsibilities.
 */
public class GuardrailTripwireException extends AgentsException
{

    /**
     * Initializes GuardrailTripwireException with required runtime dependencies and options.
     * @param phase phase.
     * @param reason human-readable reason.
     */
    public GuardrailTripwireException(String phase, String reason)
    {
        super("Guardrail triggered at " + phase + ": " + reason);
    }
}
