package stark.dataworks.coderaider.gundam.core.session;

import java.util.Optional;

/**
 * SessionStore implements session persistence and restoration.
 */
public interface ISessionStore
{

    /**
     * Performs save as part of SessionStore runtime responsibilities.
     * @param session The session used by this operation.
     */
    void save(Session session);

    /**
     * Performs load as part of SessionStore runtime responsibilities.
     * @param sessionId The session id used by this operation.
     * @return The value produced by this operation.
     */

    Optional<Session> load(String sessionId);
}
