package stark.dataworks.coderaider.gundam.core.guardrail;

import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;
/**
 * Interface OutputGuardrail.
 */

public interface OutputGuardrail
{
    /**
     * Executes evaluate.
     */
    GuardrailDecision evaluate(ExecutionContext context, LlmResponse response);
}
