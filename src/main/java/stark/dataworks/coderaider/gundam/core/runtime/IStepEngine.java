package stark.dataworks.coderaider.gundam.core.runtime;
/**
 * Interface IStepEngine.
 */

public interface IStepEngine
{
    /**
     * Executes run.
     */
    AgentRunResult run(ExecutionContext context, String userInput);
}
