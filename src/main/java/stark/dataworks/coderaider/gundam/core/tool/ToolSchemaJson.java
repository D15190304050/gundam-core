package stark.dataworks.coderaider.gundam.core.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ToolSchemaJson implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public final class ToolSchemaJson
{

    /**
     * Performs tool schema json as part of ToolSchemaJson runtime responsibilities.
     */
    private ToolSchemaJson()
    {
    }

    /**
     * Performs to json schema as part of ToolSchemaJson runtime responsibilities.
     * @param definition The definition used by this operation.
     * @return The value produced by this operation.
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
