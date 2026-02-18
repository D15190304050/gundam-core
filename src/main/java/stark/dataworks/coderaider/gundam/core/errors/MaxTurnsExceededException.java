package stark.dataworks.coderaider.gundam.core.errors;
/**
 * Class MaxTurnsExceededException.
 */

public class MaxTurnsExceededException extends AgentsException
{
    /**
     * Creates a new MaxTurnsExceededException instance.
     */
    public MaxTurnsExceededException(int maxTurns)
    {
        super("Max turns exceeded: " + maxTurns);
    }
}
