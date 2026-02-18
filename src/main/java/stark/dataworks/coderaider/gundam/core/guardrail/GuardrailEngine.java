package stark.dataworks.coderaider.gundam.core.guardrail;

import java.util.ArrayList;
import java.util.List;

import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;
/**
 * Class GuardrailEngine.
 */

public class GuardrailEngine
{
    /**
     * Field inputGuardrails.
     */
    private final List<InputGuardrail> inputGuardrails = new ArrayList<>();
    /**
     * Field outputGuardrails.
     */
    private final List<OutputGuardrail> outputGuardrails = new ArrayList<>();
    /**
     * Executes registerInput.
     */

    public void registerInput(InputGuardrail guardrail)
    {
        inputGuardrails.add(guardrail);
    }
    /**
     * Executes registerOutput.
     */

    public void registerOutput(OutputGuardrail guardrail)
    {
        outputGuardrails.add(guardrail);
    }
    /**
     * Executes evaluateInput.
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
     * Executes evaluateOutput.
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
