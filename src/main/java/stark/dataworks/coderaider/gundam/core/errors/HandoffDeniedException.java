package stark.dataworks.coderaider.gundam.core.errors;

/**
 * HandoffDeniedException implements core runtime responsibilities.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class HandoffDeniedException extends AgentsException
{

    /**
     * Performs handoff denied exception as part of HandoffDeniedException runtime responsibilities.
     * @param from The from used by this operation.
     * @param to The to used by this operation.
     */
    public HandoffDeniedException(String from, String to)
    {
        super("Handoff denied from " + from + " to " + to);
    }
}
