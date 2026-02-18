package stark.dataworks.coderaider.gundam.core.runtime;

public interface IStepEngine {
    AgentRunResult run(ExecutionContext context, String userInput);
}
