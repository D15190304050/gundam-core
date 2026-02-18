package stark.dataworks.coderaider.gundam.core.handoff;

import java.util.Objects;
/**
 * Class Handoff.
 */

public class Handoff
{
    /**
     * Field fromAgentId.
     */
    private final String fromAgentId;
    /**
     * Field toAgentId.
     */
    private final String toAgentId;
    /**
     * Field reason.
     */
    private final String reason;
    /**
     * Creates a new Handoff instance.
     */

    public Handoff(String fromAgentId, String toAgentId, String reason)
    {
        this.fromAgentId = Objects.requireNonNull(fromAgentId, "fromAgentId");
        this.toAgentId = Objects.requireNonNull(toAgentId, "toAgentId");
        this.reason = reason == null ? "" : reason;
    }
    /**
     * Executes getFromAgentId.
     */

    public String getFromAgentId()
    {
        return fromAgentId;
    }
    /**
     * Executes getToAgentId.
     */

    public String getToAgentId()
    {
        return toAgentId;
    }
    /**
     * Executes getReason.
     */

    public String getReason()
    {
        return reason;
    }
}
