package stark.dataworks.coderaider.gundam.core.runerror;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Class RunErrorHandlers.
 */

public class RunErrorHandlers
{
    /**
     * Field handlers.
     */
    private final Map<RunErrorKind, RunErrorHandler> handlers = new ConcurrentHashMap<>();
    /**
     * Executes register.
     */

    public void register(RunErrorKind kind, RunErrorHandler handler)
    {
        handlers.put(kind, handler);
    }
    /**
     * Executes get.
     */

    public Optional<RunErrorHandler> get(RunErrorKind kind)
    {
        return Optional.ofNullable(handlers.get(kind));
    }
}
