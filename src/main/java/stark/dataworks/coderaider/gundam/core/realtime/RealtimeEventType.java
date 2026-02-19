package stark.dataworks.coderaider.gundam.core.realtime;

/**
 * Event categories for realtime model sessions.
 */
public enum RealtimeEventType
{
    SESSION_STARTED,
    INPUT_RECEIVED,
    MODEL_DELTA,
    TOOL_CALL,
    HANDOFF,
    SESSION_COMPLETED,
    SESSION_FAILED
}
