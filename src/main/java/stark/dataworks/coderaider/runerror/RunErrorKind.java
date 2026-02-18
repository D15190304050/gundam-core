package stark.dataworks.coderaider.runerror;

public enum RunErrorKind {
    MAX_TURNS,
    INPUT_GUARDRAIL,
    OUTPUT_GUARDRAIL,
    MODEL_INVOCATION,
    HANDOFF,
    TOOL_EXECUTION,
    UNKNOWN
}
