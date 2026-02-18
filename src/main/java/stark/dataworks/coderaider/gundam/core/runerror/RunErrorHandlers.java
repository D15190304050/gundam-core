package stark.dataworks.coderaider.gundam.core.runerror;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RunErrorHandlers implements error classification and handler dispatch.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class RunErrorHandlers
{

    /**
     * Internal state for handlers used while coordinating runtime behavior.
     */
    private final Map<RunErrorKind, RunErrorHandler> handlers = new ConcurrentHashMap<>();

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param kind The kind used by this operation.
     * @param handler The handler used by this operation.
     */
    public void register(RunErrorKind kind, RunErrorHandler handler)
    {
        handlers.put(kind, handler);
    }

    /**
     * Returns the value requested by the caller from this RunErrorHandlers.
     * @param kind The kind used by this operation.
     * @return The value produced by this operation.
     */
    public Optional<RunErrorHandler> get(RunErrorKind kind)
    {
        return Optional.ofNullable(handlers.get(kind));
    }
}
