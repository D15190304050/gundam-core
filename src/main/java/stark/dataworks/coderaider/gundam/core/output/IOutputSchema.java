package stark.dataworks.coderaider.gundam.core.output;

import java.util.Map;

/**
 * OutputSchema implements structured output schema validation.
 */
public interface IOutputSchema
{

    /**
     * Returns the schema name.
     * @return Tool execution output returned by the MCP server.
     */
    String name();

    /**
     * Returns required field names for this schema.
     * @return Map containing operation results.
     */

    Map<String, String> requiredFields();
}
