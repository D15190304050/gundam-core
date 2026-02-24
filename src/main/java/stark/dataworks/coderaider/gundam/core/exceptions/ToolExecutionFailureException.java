package stark.dataworks.coderaider.gundam.core.exceptions;

/**
 * ToolExecutionFailureException implements core runtime responsibilities.
 */
public class ToolExecutionFailureException extends AgentsException
{

    /**
     * Performs tool execution failure exception as part of ToolExecutionFailureException runtime responsibilities.
     * @param tool The tool used by this operation.
     * @param cause The cause used by this operation.
     */
    public ToolExecutionFailureException(String tool, Throwable cause)
    {
        super("Tool execution failed: " + tool, cause);
    }
}
