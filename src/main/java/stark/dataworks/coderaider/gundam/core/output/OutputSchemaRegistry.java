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
 * Structured-output schemas keyed by schema name.
     */
    private final Map<String, IOutputSchema> schemas = new ConcurrentHashMap<>();

    /**
     * Registers this value for later lookup and execution.
     * @param schema schema definition.
     */
    public void register(IOutputSchema schema)
    {
        schemas.put(schema.name(), schema);
    }

    /**
     * Returns the current value for this object.
     * @param name human-readable name.
     * @return Optional ioutput schema value.
     */
    public Optional<IOutputSchema> get(String name)
    {
        return Optional.ofNullable(schemas.get(name));
    }
}
