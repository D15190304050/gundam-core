package stark.dataworks.coderaider.gundam.core.errors;

/**
 * ModelInvocationException implements core runtime responsibilities.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class ModelInvocationException extends AgentsException
{

    /**
     * Performs model invocation exception as part of ModelInvocationException runtime responsibilities.
     * @param message The message used by this operation.
     * @param cause The cause used by this operation.
     */
    public ModelInvocationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
