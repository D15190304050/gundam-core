package stark.dataworks.coderaider.gundam.core.handoff;

/**
 * HandoffFilter implements agent transfer rules between specialized agents.
 */
public interface IHandoffFilter
{

    /**
     * Performs allow as part of HandoffFilter runtime responsibilities.
     * @param handoff The handoff used by this operation.
     * @return {@code true} when the condition is satisfied; otherwise {@code false}.
     */
    boolean allow(Handoff handoff);
}
