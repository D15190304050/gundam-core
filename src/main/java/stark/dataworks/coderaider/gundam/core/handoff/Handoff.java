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
     * Performs handoff as part of Handoff runtime responsibilities.
     * @param fromAgentId The from agent id used by this operation.
     * @param toAgentId The to agent id used by this operation.
     * @param reason The reason used by this operation.
     */
    public Handoff(String fromAgentId, String toAgentId, String reason)
    {
        this.fromAgentId = Objects.requireNonNull(fromAgentId, "fromAgentId");
        this.toAgentId = Objects.requireNonNull(toAgentId, "toAgentId");
        this.reason = reason == null ? "" : reason;
    }
}
