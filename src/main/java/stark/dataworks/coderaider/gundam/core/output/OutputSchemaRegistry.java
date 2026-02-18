package stark.dataworks.coderaider.gundam.core.output;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Class OutputSchemaRegistry.
 */

public class OutputSchemaRegistry
{
    /**
     * Field schemas.
     */
    private final Map<String, OutputSchema> schemas = new ConcurrentHashMap<>();
    /**
     * Executes register.
     */

    public void register(OutputSchema schema)
    {
        schemas.put(schema.name(), schema);
    }
    /**
     * Executes get.
     */

    public Optional<OutputSchema> get(String name)
    {
        return Optional.ofNullable(schemas.get(name));
    }
}
