package stark.dataworks.coderaider.gundam.core.runerror;

import java.util.Objects;
/**
 * Class RunErrorData.
 */

public class RunErrorData
{
    /**
     * Field kind.
     */
    private final RunErrorKind kind;
    /**
     * Field message.
     */
    private final String message;
    /**
     * Field cause.
     */
    private final Throwable cause;
    /**
     * Creates a new RunErrorData instance.
     */

    public RunErrorData(RunErrorKind kind, String message, Throwable cause)
    {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.message = message == null ? "" : message;
        this.cause = cause;
    }
    /**
     * Executes getKind.
     */

    public RunErrorKind getKind()
    {
        return kind;
    }
    /**
     * Executes getMessage.
     */

    public String getMessage()
    {
        return message;
    }
    /**
     * Executes getCause.
     */

    public Throwable getCause()
    {
        return cause;
    }
}
