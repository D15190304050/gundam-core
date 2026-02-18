package stark.dataworks.coderaider.gundam.core.runerror;

/**
 * RunErrorHandlerResult implements error classification and handler dispatch.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class RunErrorHandlerResult
{

    /**
     * Internal state for handled; used while coordinating runtime behavior.
     */
    private final boolean handled;

    /**
     * Internal state for final output; used while coordinating runtime behavior.
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

    /**
     * Reports whether handled is currently satisfied.
     * @return {@code true} when the condition is satisfied; otherwise {@code false}.
     */
    public boolean isHandled()
    {
        return handled;
    }

    /**
     * Returns the current final output value maintained by this RunErrorHandlerResult.
     * @return The value produced by this operation.
     */
    public String getFinalOutput()
    {
        return finalOutput;
    }
}
