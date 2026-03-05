package stark.dataworks.coderaider.gundam.core.session;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InMemorySessionStore implements session persistence and restoration.
 */
public class InMemorySessionStore implements ISessionStore
{

    /**
 * In-memory session storage keyed by session id.
     */
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    /**
     * Persists the supplied value to storage.
     * @param session session.
     */
    @Override
    public void save(Session session)
    {
        sessions.put(session.getId(), session);
    }

    /**
     * Loads and returns the requested value from storage.
     * @param sessionId session identifier used to resume conversation state.
     * @return Optional session value.
     */
    @Override
    public Optional<Session> load(String sessionId)
    {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}
