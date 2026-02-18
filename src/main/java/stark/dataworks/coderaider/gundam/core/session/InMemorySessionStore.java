package stark.dataworks.coderaider.gundam.core.session;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Class InMemorySessionStore.
 */

public class InMemorySessionStore implements SessionStore
{
    /**
     * Field sessions.
     */
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    /**
     * Executes save.
     */
    @Override
    public void save(Session session)
    {
        sessions.put(session.getId(), session);
    }

    /**
     * Executes load.
     */
    @Override
    public Optional<Session> load(String sessionId)
    {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}
