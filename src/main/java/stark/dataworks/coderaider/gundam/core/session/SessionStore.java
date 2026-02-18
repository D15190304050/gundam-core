package stark.dataworks.coderaider.gundam.core.session;

import java.util.Optional;
/**
 * Interface SessionStore.
 */

public interface SessionStore
{
    /**
     * Executes save.
     */
    void save(Session session);
    /**
     * Executes load.
     */

    Optional<Session> load(String sessionId);
}
