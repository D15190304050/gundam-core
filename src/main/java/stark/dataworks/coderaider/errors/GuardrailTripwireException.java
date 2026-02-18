package stark.dataworks.coderaider.errors;

public class GuardrailTripwireException extends AgentsException {
    public GuardrailTripwireException(String phase, String reason) {
        super("Guardrail triggered at " + phase + ": " + reason);
    }
}
