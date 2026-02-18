package stark.dataworks.coderaider.gundam.core.model;

/**
 * Role implements core runtime responsibilities.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public enum Role
{
    SYSTEM,
    USER,
    ASSISTANT,
    TOOL
}
