package stark.dataworks.coderaider.gundam.core.errors;
/**
 * Class HandoffDeniedException.
 */

public class HandoffDeniedException extends AgentsException
{
    /**
     * Creates a new HandoffDeniedException instance.
     */
    public HandoffDeniedException(String from, String to)
    {
        super("Handoff denied from " + from + " to " + to);
    }
}
