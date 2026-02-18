package stark.dataworks.coderaider.guardrail;

import stark.dataworks.coderaider.runtime.ExecutionContext;

public interface InputGuardrail {
    GuardrailDecision evaluate(ExecutionContext context, String input);
}
