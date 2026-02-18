package stark.dataworks.coderaider.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ToolSchemaJson {
    private ToolSchemaJson() {
    }

    public static Map<String, Object> toJsonSchema(ToolDefinition definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        List<String> required = new java.util.ArrayList<>();
        for (ToolParameterSchema parameter : definition.getParameters()) {
            Map<String, Object> field = new HashMap<>();
            field.put("type", parameter.getType());
            field.put("description", parameter.getDescription());
            properties.put(parameter.getName(), field);
            if (parameter.isRequired()) {
                required.add(parameter.getName());
            }
        }

        schema.put("properties", properties);
        schema.put("required", required);
        return schema;
    }
}
