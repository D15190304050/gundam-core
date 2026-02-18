package stark.dataworks.coderaider.gundam.core.guardrail;

import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;

/**
 * OutputGuardrail implements input/output policy evaluation around model responses.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface OutputGuardrail
{

    /**
     * Performs evaluate as part of OutputGuardrail runtime responsibilities.
     * @param context The context used by this operation.
     * @param response The response used by this operation.
     * @return The value produced by this operation.
     */
    GuardrailDecision evaluate(ExecutionContext context, LlmResponse response);
}
