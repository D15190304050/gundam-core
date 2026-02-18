package stark.dataworks.coderaider.output;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OutputSchemaRegistry {
    private final Map<String, OutputSchema> schemas = new ConcurrentHashMap<>();

    public void register(OutputSchema schema) {
        schemas.put(schema.name(), schema);
    }

    public Optional<OutputSchema> get(String name) {
        return Optional.ofNullable(schemas.get(name));
    }
}
