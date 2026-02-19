package stark.dataworks.coderaider.gundam.core.output;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OutputSchemaRegistry implements structured output schema validation.
 */
public class OutputSchemaRegistry
{

    /**
     * Internal state for schemas used while coordinating runtime behavior.
     */
    private final Map<String, IOutputSchema> schemas = new ConcurrentHashMap<>();

    /**
     * Registers the supplied value so it can be discovered by subsequent runtime lookups.
     * @param schema The schema used by this operation.
     */
    public void register(IOutputSchema schema)
    {
        schemas.put(schema.name(), schema);
    }

    /**
     * Returns the value requested by the caller from this OutputSchemaRegistry.
     * @param name The name used by this operation.
     * @return The value produced by this operation.
     */
    public Optional<IOutputSchema> get(String name)
    {
        return Optional.ofNullable(schemas.get(name));
    }
}
