package stark.dataworks.coderaider.gundam.core.handoff;

import lombok.Getter;

import java.util.Objects;

/**
 * Handoff implements agent transfer rules between specialized agents.
 */
@Getter
public class Handoff
{

    /**
     * From agent id.
     */
    private final String fromAgentId;

    /**
     * To agent id.
     */
    private final String toAgentId;

    /**
     * Reason why execution is allowed or blocked.
     */
    private final String reason;

    /**
     * Initializes Handoff with required runtime dependencies and options.
     * @param fromAgentId from agent id.
     * @param toAgentId to agent id.
     * @param reason human-readable reason.
     */
    public Handoff(String fromAgentId, String toAgentId, String reason)
    {
        this.fromAgentId = Objects.requireNonNull(fromAgentId, "fromAgentId");
        this.toAgentId = Objects.requireNonNull(toAgentId, "toAgentId");
        this.reason = reason == null ? "" : reason;
    }
}
