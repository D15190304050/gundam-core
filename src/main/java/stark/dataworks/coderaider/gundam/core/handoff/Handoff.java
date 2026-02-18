package stark.dataworks.coderaider.gundam.core.handoff;

import java.util.Objects;

/**
 * Handoff implements agent transfer rules between specialized agents.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class Handoff
{

    /**
     * Internal state for from agent id; used while coordinating runtime behavior.
     */
    private final String fromAgentId;

    /**
     * Internal state for to agent id; used while coordinating runtime behavior.
     */
    private final String toAgentId;

    /**
     * Internal state for reason; used while coordinating runtime behavior.
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

    /**
     * Returns the current from agent id value maintained by this Handoff.
     * @return The value produced by this operation.
     */
    public String getFromAgentId()
    {
        return fromAgentId;
    }

    /**
     * Returns the current to agent id value maintained by this Handoff.
     * @return The value produced by this operation.
     */
    public String getToAgentId()
    {
        return toAgentId;
    }

    /**
     * Returns the current reason value maintained by this Handoff.
     * @return The value produced by this operation.
     */
    public String getReason()
    {
        return reason;
    }
}
