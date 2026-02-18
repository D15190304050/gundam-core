package stark.dataworks.coderaider.guardrail;

import java.util.ArrayList;
import java.util.List;
import stark.dataworks.coderaider.llmspi.LlmResponse;
import stark.dataworks.coderaider.runtime.ExecutionContext;

public class GuardrailEngine {
    private final List<InputGuardrail> inputGuardrails = new ArrayList<>();
    private final List<OutputGuardrail> outputGuardrails = new ArrayList<>();

    public void registerInput(InputGuardrail guardrail) {
        inputGuardrails.add(guardrail);
    }

    public void registerOutput(OutputGuardrail guardrail) {
        outputGuardrails.add(guardrail);
    }

    public GuardrailDecision evaluateInput(ExecutionContext context, String input) {
        for (InputGuardrail guardrail : inputGuardrails) {
            GuardrailDecision decision = guardrail.evaluate(context, input);
            if (!decision.isAllowed()) {
                return decision;
            }
        }
        return GuardrailDecision.allow();
    }

    public GuardrailDecision evaluateOutput(ExecutionContext context, LlmResponse response) {
        for (OutputGuardrail guardrail : outputGuardrails) {
            GuardrailDecision decision = guardrail.evaluate(context, response);
            if (!decision.isAllowed()) {
                return decision;
            }
        }
        return GuardrailDecision.allow();
    }
}
