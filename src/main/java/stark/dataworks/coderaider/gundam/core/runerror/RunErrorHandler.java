package stark.dataworks.coderaider.gundam.core.runerror;

/**
 * RunErrorHandler implements error classification and handler dispatch.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface RunErrorHandler
{

    /**
     * Performs on error as part of RunErrorHandler runtime responsibilities.
     * @param errorData The error data used by this operation.
     * @return The value produced by this operation.
     */
    RunErrorHandlerResult onError(RunErrorData errorData);
}
