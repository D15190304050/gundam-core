package stark.dataworks.coderaider.output;

import java.util.Map;

public class OutputValidator {
    public OutputValidationResult validate(Map<String, Object> structuredOutput, OutputSchema schema) {
        if (schema == null) {
            return OutputValidationResult.ok();
        }
        if (structuredOutput == null) {
            return OutputValidationResult.fail("Structured output missing");
        }
        for (Map.Entry<String, String> required : schema.requiredFields().entrySet()) {
            Object value = structuredOutput.get(required.getKey());
            if (value == null) {
                return OutputValidationResult.fail("Missing field: " + required.getKey());
            }
            if (!isTypeMatch(value, required.getValue())) {
                return OutputValidationResult.fail("Field type mismatch: " + required.getKey());
            }
        }
        return OutputValidationResult.ok();
    }

    private boolean isTypeMatch(Object value, String type) {
        return switch (type) {
            case "string" -> value instanceof String;
            case "number" -> value instanceof Number;
            case "boolean" -> value instanceof Boolean;
            case "object" -> value instanceof Map<?, ?>;
            default -> true;
        };
    }
}
