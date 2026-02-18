package stark.dataworks.coderaider.gundam.core.runtime;

/**
 * IStepEngine implements single-step execution that binds model calls, tool calls, and memory updates.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface IStepEngine
{

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param context The context used by this operation.
     * @param userInput The user input used by this operation.
     * @return The value produced by this operation.
     */
    AgentRunResult run(ExecutionContext context, String userInput);
}
