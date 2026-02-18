package stark.dataworks.coderaider.gundam.core.guardrail;

import java.util.ArrayList;
import java.util.List;

import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;

/**
 * GuardrailEngine implements input/output policy evaluation around model responses.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class GuardrailEngine
{

    /**
     * Internal state for input guardrails used while coordinating runtime behavior.
     */
    private final List<InputGuardrail> inputGuardrails = new ArrayList<>();

    /**
     * Internal state for output guardrails used while coordinating runtime behavior.
     */
    private final List<OutputGuardrail> outputGuardrails = new ArrayList<>();

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param guardrail The guardrail used by this operation.
     */
    public void registerInput(InputGuardrail guardrail)
    {
        inputGuardrails.add(guardrail);
    }

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param guardrail The guardrail used by this operation.
     */
    public void registerOutput(OutputGuardrail guardrail)
    {
        outputGuardrails.add(guardrail);
    }

    /**
     * Performs evaluate input as part of GuardrailEngine runtime responsibilities.
     * @param context The context used by this operation.
     * @param input The input used by this operation.
     * @return The value produced by this operation.
     */
    public GuardrailDecision evaluateInput(ExecutionContext context, String input)
    {
        for (InputGuardrail guardrail : inputGuardrails)
        {
            GuardrailDecision decision = guardrail.evaluate(context, input);
            if (!decision.isAllowed())
            {
                return decision;
            }
        }
        return GuardrailDecision.allow();
    }

    /**
     * Performs evaluate output as part of GuardrailEngine runtime responsibilities.
     * @param context The context used by this operation.
     * @param response The response used by this operation.
     * @return The value produced by this operation.
     */
    public GuardrailDecision evaluateOutput(ExecutionContext context, LlmResponse response)
    {
        for (OutputGuardrail guardrail : outputGuardrails)
        {
            GuardrailDecision decision = guardrail.evaluate(context, response);
            if (!decision.isAllowed())
            {
                return decision;
            }
        }
        return GuardrailDecision.allow();
    }
}
