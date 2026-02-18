package stark.dataworks.coderaider.gundam.core.session;

import java.util.Optional;

public interface SessionStore {
    void save(Session session);

    Optional<Session> load(String sessionId);
}
