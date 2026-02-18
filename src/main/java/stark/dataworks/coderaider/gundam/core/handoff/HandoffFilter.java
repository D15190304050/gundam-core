package stark.dataworks.coderaider.gundam.core.handoff;

/**
 * HandoffFilter implements agent transfer rules between specialized agents.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface HandoffFilter
{

    /**
     * Performs allow as part of HandoffFilter runtime responsibilities.
     * @param handoff The handoff used by this operation.
     * @return {@code true} when the condition is satisfied; otherwise {@code false}.
     */
    boolean allow(Handoff handoff);
}
