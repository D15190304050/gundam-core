package stark.dataworks.coderaider.genericagent.core.guardrail;

import stark.dataworks.coderaider.genericagent.core.runner.AgentRunnerContext;

/**
 * InputGuardrail implements input/output policy evaluation around model responses.
 */
public interface IInputGuardrail
{

    /**
     * Evaluates the supplied data and returns a decision result.
     *
     * @param context execution context.
     * @param input   input payload.
     * @return guardrail decision result.
     */
    GuardrailDecision evaluate(AgentRunnerContext context, String input);
}
