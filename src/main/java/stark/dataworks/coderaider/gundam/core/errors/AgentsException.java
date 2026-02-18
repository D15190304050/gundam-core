package stark.dataworks.coderaider.gundam.core.errors;
/**
 * Class AgentsException.
 */

public class AgentsException extends RuntimeException
{
    /**
     * Creates a new AgentsException instance.
     */
    public AgentsException(String message)
    {
        super(message);
    }
    /**
     * Creates a new AgentsException instance.
     */

    public AgentsException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
