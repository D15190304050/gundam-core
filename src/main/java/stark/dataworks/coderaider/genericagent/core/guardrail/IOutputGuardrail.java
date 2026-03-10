package stark.dataworks.coderaider.genericagent.core.guardrail;

import stark.dataworks.coderaider.genericagent.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.genericagent.core.runner.AgentRunnerContext;

/**
 * OutputGuardrail implements input/output policy evaluation around model responses.
 */
public interface IOutputGuardrail
{

    /**
     * Evaluates the supplied data and returns a decision result.
     *
     * @param context  execution context.
     * @param response response payload.
     * @return guardrail decision result.
     */
    GuardrailDecision evaluate(AgentRunnerContext context, LlmResponse response);
}
