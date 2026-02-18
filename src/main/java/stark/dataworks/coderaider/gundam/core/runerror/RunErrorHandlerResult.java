package stark.dataworks.coderaider.gundam.core.runerror;
/**
 * Class RunErrorHandlerResult.
 */

public class RunErrorHandlerResult
{
    /**
     * Field handled.
     */
    private final boolean handled;
    /**
     * Field finalOutput.
     */
    private final String finalOutput;
    /**
     * Creates a new RunErrorHandlerResult instance.
     */

    private RunErrorHandlerResult(boolean handled, String finalOutput)
    {
        this.handled = handled;
        this.finalOutput = finalOutput;
    }
    /**
     * Executes notHandled.
     */

    public static RunErrorHandlerResult notHandled()
    {
        return new RunErrorHandlerResult(false, null);
    }
    /**
     * Executes handled.
     */

    public static RunErrorHandlerResult handled(String finalOutput)
    {
        return new RunErrorHandlerResult(true, finalOutput);
    }
    /**
     * Executes isHandled.
     */

    public boolean isHandled()
    {
        return handled;
    }
    /**
     * Executes getFinalOutput.
     */

    public String getFinalOutput()
    {
        return finalOutput;
    }
}
