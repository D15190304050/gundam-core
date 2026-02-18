package stark.dataworks.coderaider.gundam.core.guardrail;

import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;
/**
 * Interface InputGuardrail.
 */

public interface InputGuardrail
{
    /**
     * Executes evaluate.
     */
    GuardrailDecision evaluate(ExecutionContext context, String input);
}
