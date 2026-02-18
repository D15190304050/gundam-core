package stark.dataworks.coderaider.gundam.core.output;

import java.util.Map;
/**
 * Interface OutputSchema.
 */

public interface OutputSchema
{
    /**
     * Executes name.
     */
    String name();
    /**
     * Executes requiredFields.
     */

    Map<String, String> requiredFields();
}
