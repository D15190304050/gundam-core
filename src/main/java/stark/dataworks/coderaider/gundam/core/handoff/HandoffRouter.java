package stark.dataworks.coderaider.gundam.core.handoff;

import java.util.ArrayList;
import java.util.List;

/**
 * HandoffRouter implements agent transfer rules between specialized agents.
 */
public class HandoffRouter
{

    /**
     * Internal state for filters used while coordinating runtime behavior.
     */
    private final List<IHandoffFilter> filters = new ArrayList<>();

    /**
     * Adds data to internal state consumed by later runtime steps.
     * @param filter The filter used by this operation.
     */
    public void addFilter(IHandoffFilter filter)
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
        for (IHandoffFilter filter : filters)
        {
            if (!filter.allow(handoff))
            {
                return false;
            }
        }
        return true;
    }
}
