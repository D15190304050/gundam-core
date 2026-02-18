package stark.dataworks.coderaider.gundam.core.handoff;

import java.util.ArrayList;
import java.util.List;
/**
 * Class HandoffRouter.
 */

public class HandoffRouter
{
    /**
     * Field filters.
     */
    private final List<HandoffFilter> filters = new ArrayList<>();
    /**
     * Executes addFilter.
     */

    public void addFilter(HandoffFilter filter)
    {
        filters.add(filter);
    }
    /**
     * Executes canRoute.
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
