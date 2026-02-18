package stark.dataworks.coderaider.gundam.core.session;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InMemorySessionStore implements session persistence and restoration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class InMemorySessionStore implements SessionStore
{

    /**
     * Internal state for sessions used while coordinating runtime behavior.
     */
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    /**
     * Performs save as part of InMemorySessionStore runtime responsibilities.
     * @param session The session used by this operation.
     */
    @Override
    public void save(Session session)
    {
        sessions.put(session.getId(), session);
    }

    /**
     * Performs load as part of InMemorySessionStore runtime responsibilities.
     * @param sessionId The session id used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public Optional<Session> load(String sessionId)
    {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}
