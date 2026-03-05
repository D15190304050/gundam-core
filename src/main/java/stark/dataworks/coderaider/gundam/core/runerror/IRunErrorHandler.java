package stark.dataworks.coderaider.gundam.core.runerror;

/**
 * RunErrorHandler implements error classification and handler dispatch.
 */
public interface IRunErrorHandler
{

    /**
     * Handles a classified run error.
     * @param errorData error data.
     * @return run error handler result.
     */
    RunErrorHandlerResult onError(RunErrorData errorData);
}
