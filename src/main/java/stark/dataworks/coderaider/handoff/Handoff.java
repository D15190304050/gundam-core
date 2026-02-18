package stark.dataworks.coderaider.handoff;

import java.util.Objects;

public class Handoff {
    private final String fromAgentId;
    private final String toAgentId;
    private final String reason;

    public Handoff(String fromAgentId, String toAgentId, String reason) {
        this.fromAgentId = Objects.requireNonNull(fromAgentId, "fromAgentId");
        this.toAgentId = Objects.requireNonNull(toAgentId, "toAgentId");
        this.reason = reason == null ? "" : reason;
    }

    public String getFromAgentId() {
        return fromAgentId;
    }

    public String getToAgentId() {
        return toAgentId;
    }

    public String getReason() {
        return reason;
    }
}
