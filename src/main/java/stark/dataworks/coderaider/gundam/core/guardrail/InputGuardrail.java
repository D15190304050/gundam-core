package stark.dataworks.coderaider.gundam.core.guardrail;

import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;

/**
 * InputGuardrail implements input/output policy evaluation around model responses.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface InputGuardrail
{

    /**
     * Performs evaluate as part of InputGuardrail runtime responsibilities.
     * @param context The context used by this operation.
     * @param input The input used by this operation.
     * @return The value produced by this operation.
     */
    GuardrailDecision evaluate(ExecutionContext context, String input);
}
