package stark.dataworks.coderaider.guardrail;

import java.util.Objects;

public class GuardrailDecision {
    private final boolean allowed;
    private final String reason;

    private GuardrailDecision(boolean allowed, String reason) {
        this.allowed = allowed;
        this.reason = reason == null ? "" : reason;
    }

    public static GuardrailDecision allow() {
        return new GuardrailDecision(true, "");
    }

    public static GuardrailDecision deny(String reason) {
        return new GuardrailDecision(false, Objects.requireNonNull(reason, "reason"));
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getReason() {
        return reason;
    }
}
