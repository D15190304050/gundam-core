package stark.dataworks.coderaider.gundam.core.output;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds output-schema contracts from Java classes for structured output generation.
 */
public final class OutputSchemaMapper
{
    private OutputSchemaMapper()
    {
    }

    public static IOutputSchema fromClass(Class<?> outputType)
    {
        if (outputType == null)
        {
            throw new IllegalArgumentException("outputType must not be null");
        }
        Map<String, String> fields = new LinkedHashMap<>();
        for (Field field : outputType.getDeclaredFields())
        {
            if (Modifier.isStatic(field.getModifiers()))
            {
                continue;
            }
            fields.put(field.getName(), typeName(field.getType()));
        }
        return new ClassOutputSchema(outputType.getSimpleName(), fields);
    }

    public static Map<String, Object> toOpenAiJsonSchema(Class<?> outputType)
    {
        IOutputSchema schema = fromClass(outputType);
        Map<String, Object> properties = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : schema.requiredFields().entrySet())
        {
            properties.put(entry.getKey(), Map.of("type", entry.getValue()));
        }
        return Map.of(
            "name", schema.name(),
            "schema", Map.of(
                "type", "object",
                "properties", properties,
                "required", List.copyOf(schema.requiredFields().keySet()),
                "additionalProperties", false),
            "strict", true);
    }

    private static String typeName(Class<?> type)
    {
        if (type == String.class || type.isEnum() || type == Character.class || type == char.class)
        {
            return "string";
        }
        if (Number.class.isAssignableFrom(type)
            || type == byte.class
            || type == short.class
            || type == int.class
            || type == long.class
            || type == float.class
            || type == double.class)
        {
            return "number";
        }
        if (type == boolean.class || type == Boolean.class)
        {
            return "boolean";
        }
        if (List.class.isAssignableFrom(type) || type.isArray())
        {
            return "array";
        }
        if (Map.class.isAssignableFrom(type))
        {
            return "object";
        }
        return "object";
    }
}
