package stark.dataworks.coderaider.gundam.core.output;

import java.util.Map;

/**
 * OutputValidator implements structured output schema validation.
 */
public class OutputValidator
{

    /**
     * Validates this value.
     * @param Map<String map<string.
     * @param structuredOutput structured output.
     * @param schema schema definition.
     * @return output validation result.
     */
    public OutputValidationResult validate(Map<String, Object> structuredOutput, IOutputSchema schema)
    {
        if (schema == null)
        {
            return OutputValidationResult.ok();
        }
        if (structuredOutput == null)
        {
            return OutputValidationResult.fail("Structured output missing");
        }
        for (Map.Entry<String, String> required : schema.requiredFields().entrySet())
        {
            Object value = structuredOutput.get(required.getKey());
            if (value == null)
            {
                return OutputValidationResult.fail("Missing field: " + required.getKey());
            }
            if (!isTypeMatch(value, required.getValue()))
            {
                return OutputValidationResult.fail("Field type mismatch: " + required.getKey());
            }
        }
        return OutputValidationResult.ok();
    }

    /**
     * Checks whether a runtime value matches the expected schema type.
     * @param value value.
     * @param type type discriminator.
     * @return True when routing is allowed; false otherwise.
     */
    private boolean isTypeMatch(Object value, String type)
    {
        return switch (type)
        {
            case "string" -> value instanceof String;
            case "number" -> value instanceof Number;
            case "boolean" -> value instanceof Boolean;
            case "object" -> value instanceof Map<?, ?>;
            case "array" -> value instanceof java.util.List<?>;
            default -> true;
        };
    }
}
