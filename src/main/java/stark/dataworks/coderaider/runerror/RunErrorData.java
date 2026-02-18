package stark.dataworks.coderaider.runerror;

import java.util.Objects;

public class RunErrorData {
    private final RunErrorKind kind;
    private final String message;
    private final Throwable cause;

    public RunErrorData(RunErrorKind kind, String message, Throwable cause) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.message = message == null ? "" : message;
        this.cause = cause;
    }

    public RunErrorKind getKind() {
        return kind;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }
}
