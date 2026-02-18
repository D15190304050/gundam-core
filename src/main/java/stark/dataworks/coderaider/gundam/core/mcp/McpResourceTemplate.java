package stark.dataworks.coderaider.gundam.core.mcp;

/**
 * McpResourceTemplate implements MCP server integration and tool bridging.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public record McpResourceTemplate(String name, String uriTemplate, String description)
{
}
