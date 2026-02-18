package stark.dataworks.coderaider.gundam.core.errors;
/**
 * Class ToolExecutionFailureException.
 */

public class ToolExecutionFailureException extends AgentsException
{
    /**
     * Creates a new ToolExecutionFailureException instance.
     */
    public ToolExecutionFailureException(String tool, Throwable cause)
    {
        super("Tool execution failed: " + tool, cause);
    }
}
