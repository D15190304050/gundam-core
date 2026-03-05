package stark.dataworks.coderaider.gundam.core.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ToolSchemaJson implements tool contracts, schema metadata, and executable tool registration.
 */
public final class ToolSchemaJson
{

    /**
     * Initializes ToolSchemaJson with required runtime dependencies and options.
     */
    private ToolSchemaJson()
    {
    }

    /**
     * Converts a JSON schema definition into internal schema structures.
     * @param definition definition object.
     * @return Map containing operation results.
     */
    public static Map<String, Object> toJsonSchema(ToolDefinition definition)
    {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        List<String> required = new java.util.ArrayList<>();
        for (ToolParameterSchema parameter : definition.getParameters())
        {
            Map<String, Object> field = new HashMap<>();
            field.put("type", parameter.getType());
            field.put("description", parameter.getDescription());
            properties.put(parameter.getName(), field);
            if (parameter.isRequired())
            {
                required.add(parameter.getName());
            }
        }

        schema.put("properties", properties);
        schema.put("required", required);
        return schema;
    }
}
