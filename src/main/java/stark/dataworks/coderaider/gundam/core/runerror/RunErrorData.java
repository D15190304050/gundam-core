package stark.dataworks.coderaider.gundam.core.runerror;

import java.util.Objects;

/**
 * RunErrorData implements error classification and handler dispatch.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class RunErrorData
{

    /**
     * Internal state for kind; used while coordinating runtime behavior.
     */
    private final RunErrorKind kind;

    /**
     * Internal state for message; used while coordinating runtime behavior.
     */
    private final String message;

    /**
     * Internal state for cause; used while coordinating runtime behavior.
     */
    private final Throwable cause;

    /**
     * Performs run error data as part of RunErrorData runtime responsibilities.
     * @param kind The kind used by this operation.
     * @param message The message used by this operation.
     * @param cause The cause used by this operation.
     */
    public RunErrorData(RunErrorKind kind, String message, Throwable cause)
    {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.message = message == null ? "" : message;
        this.cause = cause;
    }

    /**
     * Returns the current kind value maintained by this RunErrorData.
     * @return The value produced by this operation.
     */
    public RunErrorKind getKind()
    {
        return kind;
    }

    /**
     * Returns the current message value maintained by this RunErrorData.
     * @return The value produced by this operation.
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Returns the current cause value maintained by this RunErrorData.
     * @return The value produced by this operation.
     */
    public Throwable getCause()
    {
        return cause;
    }
}
