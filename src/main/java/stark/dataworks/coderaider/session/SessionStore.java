package stark.dataworks.coderaider.session;

import java.util.Optional;

public interface SessionStore {
    void save(Session session);

    Optional<Session> load(String sessionId);
}
