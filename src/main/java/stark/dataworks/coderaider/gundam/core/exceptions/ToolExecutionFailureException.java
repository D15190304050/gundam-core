package stark.dataworks.coderaider.gundam.core.exceptions;

/**
 * ToolExecutionFailureException implements core runtime responsibilities.
 */
public class ToolExecutionFailureException extends AgentsException
{

    /**
     * Initializes ToolExecutionFailureException with required runtime dependencies and options.
     * @param tool tool instance.
     * @param cause root cause exception.
     */
    public ToolExecutionFailureException(String tool, Throwable cause)
    {
        super("Tool execution failed: " + tool, cause);
    }
}
