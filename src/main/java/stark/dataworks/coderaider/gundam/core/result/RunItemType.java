package stark.dataworks.coderaider.gundam.core.result;

/**
 * RunItemType implements normalized run result structures.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public enum RunItemType
{
    USER_MESSAGE,
    ASSISTANT_MESSAGE,
    TOOL_CALL,
    TOOL_RESULT,
    HANDOFF,
    SYSTEM_EVENT
}
