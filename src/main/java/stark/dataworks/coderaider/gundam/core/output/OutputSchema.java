package stark.dataworks.coderaider.gundam.core.output;

import java.util.Map;

/**
 * OutputSchema implements structured output schema validation.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface OutputSchema
{

    /**
     * Performs name as part of OutputSchema runtime responsibilities.
     * @return The value produced by this operation.
     */
    String name();

    /**
     * Performs required fields as part of OutputSchema runtime responsibilities.
     * @return The value produced by this operation.
     */

    Map<String, String> requiredFields();
}
