package stark.dataworks.coderaider.runerror;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RunErrorHandlers {
    private final Map<RunErrorKind, RunErrorHandler> handlers = new ConcurrentHashMap<>();

    public void register(RunErrorKind kind, RunErrorHandler handler) {
        handlers.put(kind, handler);
    }

    public Optional<RunErrorHandler> get(RunErrorKind kind) {
        return Optional.ofNullable(handlers.get(kind));
    }
}
