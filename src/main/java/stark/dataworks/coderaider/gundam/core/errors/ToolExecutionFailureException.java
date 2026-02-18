package stark.dataworks.coderaider.gundam.core.errors;

public class ToolExecutionFailureException extends AgentsException {
    public ToolExecutionFailureException(String tool, Throwable cause) {
        super("Tool execution failed: " + tool, cause);
    }
}
