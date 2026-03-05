package stark.dataworks.coderaider.gundam.core.guardrail;

import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;

/**
 * OutputGuardrail implements input/output policy evaluation around model responses.
 */
public interface IOutputGuardrail
{

    /**
     * Evaluates the supplied data and returns a decision result.
     * @param context execution context.
     * @param response response payload.
     * @return guardrail decision result.
     */
    GuardrailDecision evaluate(ExecutionContext context, LlmResponse response);
}
