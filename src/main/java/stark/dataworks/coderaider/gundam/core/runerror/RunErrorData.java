package stark.dataworks.coderaider.gundam.core.runerror;

import lombok.Getter;

import java.util.Objects;

/**
 * RunErrorData implements error classification and handler dispatch.
 */
@Getter
public class RunErrorData
{

    /**
     * Typed classification of the runtime error.
     */
    private final RunErrorKind kind;

    /**
     * Human-readable error message.
     */
    private final String message;

    /**
     * Original exception that caused this error, when available.
     */
    private final Throwable cause;

    /**
     * Initializes RunErrorData with required runtime dependencies and options.
     * @param kind kind.
     * @param message conversation message.
     * @param cause root cause exception.
     */
    public RunErrorData(RunErrorKind kind, String message, Throwable cause)
    {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.message = message == null ? "" : message;
        this.cause = cause;
    }
}
