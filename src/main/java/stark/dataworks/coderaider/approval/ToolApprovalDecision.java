package stark.dataworks.coderaider.approval;

import java.util.Objects;

public class ToolApprovalDecision {
    private final boolean approved;
    private final String reason;

    private ToolApprovalDecision(boolean approved, String reason) {
        this.approved = approved;
        this.reason = reason == null ? "" : reason;
    }

    public static ToolApprovalDecision approve() {
        return new ToolApprovalDecision(true, "");
    }

    public static ToolApprovalDecision deny(String reason) {
        return new ToolApprovalDecision(false, Objects.requireNonNull(reason, "reason"));
    }

    public boolean isApproved() {
        return approved;
    }

    public String getReason() {
        return reason;
    }
}
