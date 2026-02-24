package stark.dataworks.coderaider.gundam.core.exceptions;

/**
 * MaxTurnsExceededException implements core runtime responsibilities.
 */
public class MaxTurnsExceededException extends AgentsException
{

    /**
     * Performs max turns exceeded exception as part of MaxTurnsExceededException runtime responsibilities.
     * @param maxTurns The max turns used by this operation.
     */
    public MaxTurnsExceededException(int maxTurns)
    {
        super("Max turns exceeded: " + maxTurns);
    }
}
