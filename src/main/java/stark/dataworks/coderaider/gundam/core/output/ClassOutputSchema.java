package stark.dataworks.coderaider.gundam.core.output;

import java.util.Map;

/**
 * Output schema implementation backed by a Java class definition.
 */
public record ClassOutputSchema(String name, Map<String, String> requiredFields) implements IOutputSchema
{
}
