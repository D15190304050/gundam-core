package stark.dataworks.coderaider.gundam.core.guardrail;

import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;

public interface OutputGuardrail {
    GuardrailDecision evaluate(ExecutionContext context, LlmResponse response);
}
