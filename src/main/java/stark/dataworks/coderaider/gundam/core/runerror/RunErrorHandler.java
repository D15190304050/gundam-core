package stark.dataworks.coderaider.gundam.core.runerror;
/**
 * Interface RunErrorHandler.
 */

public interface RunErrorHandler
{
    /**
     * Executes onError.
     */
    RunErrorHandlerResult onError(RunErrorData errorData);
}
