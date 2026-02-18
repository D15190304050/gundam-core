package stark.dataworks.coderaider.gundam.core.runerror;

import lombok.Getter;

import java.util.Objects;

/**
 * RunErrorData implements error classification and handler dispatch.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
@Getter
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
}
