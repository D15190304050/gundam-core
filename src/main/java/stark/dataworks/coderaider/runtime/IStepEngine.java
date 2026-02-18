package stark.dataworks.coderaider.runtime;

public interface IStepEngine {
    AgentRunResult run(ExecutionContext context, String userInput);
}
