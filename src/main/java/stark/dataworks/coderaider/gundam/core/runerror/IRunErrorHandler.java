package stark.dataworks.coderaider.gundam.core.runerror;

/**
 * RunErrorHandler implements error classification and handler dispatch.
 */
public interface IRunErrorHandler
{

    /**
     * Performs on error as part of RunErrorHandler runtime responsibilities.
     * @param errorData The error data used by this operation.
     * @return The value produced by this operation.
     */
    RunErrorHandlerResult onError(RunErrorData errorData);
}
