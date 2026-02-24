package stark.dataworks.coderaider.gundam.core.context;

/**
 * RunItemType implements normalized run result structures.
 */
public enum ContextItemType
{
    USER_MESSAGE,
    ASSISTANT_MESSAGE,
    TOOL_CALL,
    TOOL_RESULT,
    HANDOFF,
    SYSTEM_EVENT
}
