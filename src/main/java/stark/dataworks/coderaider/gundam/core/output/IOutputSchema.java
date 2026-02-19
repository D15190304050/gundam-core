package stark.dataworks.coderaider.gundam.core.output;

import java.util.Map;

/**
 * OutputSchema implements structured output schema validation.
 */
public interface IOutputSchema
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
