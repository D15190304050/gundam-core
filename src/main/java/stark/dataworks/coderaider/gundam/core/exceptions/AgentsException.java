package stark.dataworks.coderaider.gundam.core.exceptions;

/**
 * AgentsException implements core runtime responsibilities.
 */
public class AgentsException extends RuntimeException
{

    /**
     * Initializes AgentsException with required runtime dependencies and options.
     * @param message conversation message.
     */
    public AgentsException(String message)
    {
        super(message);
    }

    /**
     * Initializes AgentsException with required runtime dependencies and options.
     * @param message conversation message.
     * @param cause root cause exception.
     */
    public AgentsException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
