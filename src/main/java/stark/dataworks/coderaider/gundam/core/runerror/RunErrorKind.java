package stark.dataworks.coderaider.gundam.core.runerror;

/**
 * RunErrorKind implements error classification and handler dispatch.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public enum RunErrorKind
{
    MAX_TURNS,
    INPUT_GUARDRAIL,
    OUTPUT_GUARDRAIL,
    MODEL_INVOCATION,
    HANDOFF,
    TOOL_EXECUTION,
    UNKNOWN
}
