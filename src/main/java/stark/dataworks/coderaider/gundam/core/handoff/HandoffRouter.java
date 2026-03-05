package stark.dataworks.coderaider.gundam.core.handoff;

import java.util.ArrayList;
import java.util.List;

/**
 * HandoffRouter implements agent transfer rules between specialized agents.
 */
public class HandoffRouter
{

    /**
 * Filters that decide whether a handoff candidate is allowed.
     */
    private final List<IHandoffFilter> filters = new ArrayList<>();

    /**
     * Registers a handoff filter used to allow or block candidate routes.
     * @param filter handoff filter to register.
     */
    public void addFilter(IHandoffFilter filter)
    {
        filters.add(filter);
    }

    /**
     * Checks whether the handoff can route to the target agent.
     * @param handoff handoff.
     * @return True when routing is allowed; false otherwise.
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
