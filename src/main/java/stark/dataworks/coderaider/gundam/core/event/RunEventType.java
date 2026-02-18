package stark.dataworks.coderaider.gundam.core.event;

/**
 * RunEventType implements run event payloads.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public enum RunEventType
{
    RUN_STARTED,
    STEP_STARTED,
    MODEL_REQUESTED,
    MODEL_RESPONSE_DELTA,
    MODEL_RESPONDED,
    TOOL_CALL_REQUESTED,
    TOOL_CALL_COMPLETED,
    HANDOFF_OCCURRED,
    GUARDRAIL_BLOCKED,
    RUN_COMPLETED,
    RUN_FAILED
}
