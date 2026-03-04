package stark.dataworks.coderaider.gundam.core.runerror;

import lombok.Getter;

/**
 * RunErrorHandlerResult implements error classification and handler dispatch.
 */
@Getter
public class RunErrorHandlerResult
{

    /**
     * Whether the error handler consumed the error.
     */
    private final boolean handled;

    /**
     * Fallback or final output produced by error handling/execution.
     */
    private final String finalOutput;

    /**
     * Performs run error handler result as part of RunErrorHandlerResult runtime responsibilities.
     * @param handled The handled used by this operation.
     * @param finalOutput The final output used by this operation.
     */
    private RunErrorHandlerResult(boolean handled, String finalOutput)
    {
        this.handled = handled;
        this.finalOutput = finalOutput;
    }

    /**
     * Performs not handled as part of RunErrorHandlerResult runtime responsibilities.
     * @return The value produced by this operation.
     */
    public static RunErrorHandlerResult notHandled()
    {
        return new RunErrorHandlerResult(false, null);
    }

    /**
     * Performs handled as part of RunErrorHandlerResult runtime responsibilities.
     * @param finalOutput The final output used by this operation.
     * @return The value produced by this operation.
     */
    public static RunErrorHandlerResult handled(String finalOutput)
    {
        return new RunErrorHandlerResult(true, finalOutput);
    }
}
