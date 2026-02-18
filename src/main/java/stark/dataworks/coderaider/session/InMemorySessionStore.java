package stark.dataworks.coderaider.session;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySessionStore implements SessionStore {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @Override
    public void save(Session session) {
        sessions.put(session.getId(), session);
    }

    @Override
    public Optional<Session> load(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}
