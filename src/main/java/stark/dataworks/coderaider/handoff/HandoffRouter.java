package stark.dataworks.coderaider.handoff;

import java.util.ArrayList;
import java.util.List;

public class HandoffRouter {
    private final List<HandoffFilter> filters = new ArrayList<>();

    public void addFilter(HandoffFilter filter) {
        filters.add(filter);
    }

    public boolean canRoute(Handoff handoff) {
        for (HandoffFilter filter : filters) {
            if (!filter.allow(handoff)) {
                return false;
            }
        }
        return true;
    }
}
