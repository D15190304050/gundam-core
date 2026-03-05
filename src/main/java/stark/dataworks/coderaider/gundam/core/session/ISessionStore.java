package stark.dataworks.coderaider.gundam.core.session;

import java.util.Optional;

/**
 * SessionStore implements session persistence and restoration.
 */
public interface ISessionStore
{

    /**
     * Persists the supplied value to storage.
     * @param session session.
     */
    void save(Session session);

    /**
     * Loads and returns the requested value from storage.
     * @param sessionId session identifier used to resume conversation state.
     * @return Optional session value.
     */

    Optional<Session> load(String sessionId);
}
