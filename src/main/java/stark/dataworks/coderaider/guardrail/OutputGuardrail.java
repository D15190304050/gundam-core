package stark.dataworks.coderaider.guardrail;

import stark.dataworks.coderaider.llmspi.LlmResponse;
import stark.dataworks.coderaider.runtime.ExecutionContext;

public interface OutputGuardrail {
    GuardrailDecision evaluate(ExecutionContext context, LlmResponse response);
}
