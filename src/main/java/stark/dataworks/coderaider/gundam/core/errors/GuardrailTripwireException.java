package stark.dataworks.coderaider.gundam.core.errors;
/**
 * Class GuardrailTripwireException.
 */

public class GuardrailTripwireException extends AgentsException
{
    /**
     * Creates a new GuardrailTripwireException instance.
     */
    public GuardrailTripwireException(String phase, String reason)
    {
        super("Guardrail triggered at " + phase + ": " + reason);
    }
}
