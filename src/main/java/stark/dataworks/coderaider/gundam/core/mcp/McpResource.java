package stark.dataworks.coderaider.gundam.core.mcp;

/**
 * McpResource implements MCP server integration and tool bridging.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public record McpResource(String uri, String mimeType, String content)
{
}
