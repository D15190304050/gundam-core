package stark.dataworks.coderaider.gundam.core.exceptions;

/**
 * ModelInvocationException implements core runtime responsibilities.
 */
public class ModelInvocationException extends AgentsException
{

    /**
     * Performs model invocation exception as part of ModelInvocationException runtime responsibilities.
     * @param message The message used by this operation.
     * @param cause The cause used by this operation.
     */
    public ModelInvocationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Returns the root cause message by traversing the exception chain.
     * @return The root cause message, or the current message if no cause exists.
     */
    public String getRootCauseMessage()
    {
        Throwable root = getCause();
        if (root == null)
        {
            return getMessage();
        }
        
        while (root.getCause() != null)
        {
            root = root.getCause();
        }
        
        return root.getMessage() != null ? root.getMessage() : getMessage();
    }

    /**
     * Returns the full error chain message including all causes.
     * @return The full error chain message.
     */
    public String getFullMessage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage());
        
        Throwable current = getCause();
        while (current != null)
        {
            sb.append(" -> ").append(current.getMessage());
            current = current.getCause();
        }
        
        return sb.toString();
    }
}
