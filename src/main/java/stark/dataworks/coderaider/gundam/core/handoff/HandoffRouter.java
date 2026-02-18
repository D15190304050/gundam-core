package stark.dataworks.coderaider.gundam.core.handoff;

import java.util.ArrayList;
import java.util.List;

/**
 * HandoffRouter implements agent transfer rules between specialized agents.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class HandoffRouter
{

    /**
     * Internal state for filters used while coordinating runtime behavior.
     */
    private final List<HandoffFilter> filters = new ArrayList<>();

    /**
     * Adds data to internal state consumed by later runtime steps.
     * @param filter The filter used by this operation.
     */
    public void addFilter(HandoffFilter filter)
    {
        filters.add(filter);
    }

    /**
     * Performs can route as part of HandoffRouter runtime responsibilities.
     * @param handoff The handoff used by this operation.
     * @return {@code true} when the condition is satisfied; otherwise {@code false}.
     */
    public boolean canRoute(Handoff handoff)
    {
        for (HandoffFilter filter : filters)
        {
            if (!filter.allow(handoff))
            {
                return false;
            }
        }
        return true;
    }
}
