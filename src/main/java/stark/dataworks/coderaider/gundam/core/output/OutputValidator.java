package stark.dataworks.coderaider.gundam.core.output;

import java.util.Map;

/**
 * OutputValidator implements structured output schema validation.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class OutputValidator
{

    /**
     * Validates  and throws when required constraints are violated.
     * @param structuredOutput The structured output used by this operation.
     * @param schema The schema used by this operation.
     * @return The value produced by this operation.
     */
    public OutputValidationResult validate(Map<String, Object> structuredOutput, OutputSchema schema)
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
     * Reports whether type match is currently satisfied.
     * @param value The value used by this operation.
     * @param type The type used by this operation.
     * @return {@code true} when the condition is satisfied; otherwise {@code false}.
     */
    private boolean isTypeMatch(Object value, String type)
    {
        return switch (type)
        {
            case "string" -> value instanceof String;
            case "number" -> value instanceof Number;
            case "boolean" -> value instanceof Boolean;
            case "object" -> value instanceof Map<?, ?>;
            default -> true;
        };
    }
}
