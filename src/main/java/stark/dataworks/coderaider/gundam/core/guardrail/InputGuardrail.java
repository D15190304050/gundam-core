package stark.dataworks.coderaider.gundam.core.guardrail;

import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;

public interface InputGuardrail {
    GuardrailDecision evaluate(ExecutionContext context, String input);
}
