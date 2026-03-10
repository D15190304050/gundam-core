package stark.dataworks.coderaider.genericagent.core.guardrail;

import java.util.ArrayList;
import java.util.List;

import stark.dataworks.coderaider.genericagent.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.genericagent.core.runner.AgentRunnerContext;

/**
 * GuardrailEngine implements input/output policy evaluation around model responses.
 */
public class GuardrailEngine
{

    /**
     * Input guardrails captured for this step.
     */
    private final List<IInputGuardrail> inputGuardrails = new ArrayList<>();

    /**
     * Output guardrails produced by this step.
     */
    private final List<IOutputGuardrail> outputGuardrails = new ArrayList<>();

    /**
     * Registers an input guardrail that runs before model invocation.
     *
     * @param guardrail guardrail implementation to register.
     */
    public void registerInput(IInputGuardrail guardrail)
    {
        inputGuardrails.add(guardrail);
    }

    /**
     * Registers an output guardrail that runs after model response generation.
     *
     * @param guardrail guardrail implementation to register.
     */
    public void registerOutput(IOutputGuardrail guardrail)
    {
        outputGuardrails.add(guardrail);
    }

    /**
     * Evaluates the input against configured guardrails to decide whether execution can continue.
     *
     * @param context execution context.
     * @param input   input payload.
     * @return guardrail decision result.
     */
    public GuardrailDecision evaluateInput(AgentRunnerContext context, String input)
    {
        for (IInputGuardrail guardrail : inputGuardrails)
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
     * Evaluates the model output against configured guardrails before downstream processing.
     *
     * @param context  execution context.
     * @param response model/tool response payload.
     * @return guardrail decision result.
     */
    public GuardrailDecision evaluateOutput(AgentRunnerContext context, LlmResponse response)
    {
        for (IOutputGuardrail guardrail : outputGuardrails)
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
