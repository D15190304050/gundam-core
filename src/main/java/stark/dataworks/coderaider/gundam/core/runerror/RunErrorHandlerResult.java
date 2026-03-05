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
     * Initializes RunErrorHandlerResult with required runtime dependencies and options.
     * @param handled handled.
     * @param finalOutput final output.
     */
    private RunErrorHandlerResult(boolean handled, String finalOutput)
    {
        this.handled = handled;
        this.finalOutput = finalOutput;
    }

    /**
     * Returns a result indicating the error was not handled.
     * @return run error handler result.
     */
    public static RunErrorHandlerResult notHandled()
    {
        return new RunErrorHandlerResult(false, null);
    }

    /**
     * Handles a streamed text delta.
     * @param finalOutput final output.
     * @return run error handler result.
     */
    public static RunErrorHandlerResult handled(String finalOutput)
    {
        return new RunErrorHandlerResult(true, finalOutput);
    }
}
