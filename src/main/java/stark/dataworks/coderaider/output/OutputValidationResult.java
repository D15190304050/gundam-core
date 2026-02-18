package stark.dataworks.coderaider.output;

public class OutputValidationResult {
    private final boolean valid;
    private final String reason;

    private OutputValidationResult(boolean valid, String reason) {
        this.valid = valid;
        this.reason = reason == null ? "" : reason;
    }

    public static OutputValidationResult ok() {
        return new OutputValidationResult(true, "");
    }

    public static OutputValidationResult fail(String reason) {
        return new OutputValidationResult(false, reason);
    }

    public boolean isValid() {
        return valid;
    }

    public String getReason() {
        return reason;
    }
}
