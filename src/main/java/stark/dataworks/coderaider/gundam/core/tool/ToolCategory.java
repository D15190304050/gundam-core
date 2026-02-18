package stark.dataworks.coderaider.gundam.core.tool;

/**
 * ToolCategory implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public enum ToolCategory
{
    FUNCTION,
    WEB_SEARCH,
    FILE_SEARCH,
    SHELL,
    CODE_INTERPRETER,
    IMAGE_GENERATION,
    MCP
}
