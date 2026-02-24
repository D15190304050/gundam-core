package stark.dataworks.coderaider.gundam.core.exceptions;

/**
 * AgentsException implements core runtime responsibilities.
 */
public class AgentsException extends RuntimeException
{

    /**
     * Performs agents exception as part of AgentsException runtime responsibilities.
     * @param message The message used by this operation.
     */
    public AgentsException(String message)
    {
        super(message);
    }

    /**
     * Performs agents exception as part of AgentsException runtime responsibilities.
     * @param message The message used by this operation.
     * @param cause The cause used by this operation.
     */
    public AgentsException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
