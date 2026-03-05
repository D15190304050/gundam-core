package stark.dataworks.coderaider.gundam.core.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AgentDefinitionLoader implements agent definitions and lookup used by runners and handoff resolution.
 */
public final class AgentDefinitionLoader
{

    /**
 * ObjectMapper used for JSON serialization/deserialization.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Initializes AgentDefinitionLoader with required runtime dependencies and options.
     */
    private AgentDefinitionLoader()
    {
    }

    /**
     * Serializes the value to JSON.
     * @param json JSON document to parse.
     * @return Agent definition associated with this instance.
     */
    public static AgentDefinition fromJson(String json)
    {
        try
        {
            AgentDefinition definition = MAPPER.readValue(json, AgentDefinition.class);
            definition.validate();
            return definition;
        }
        catch (JsonProcessingException e)
        {
            throw new IllegalArgumentException("Invalid agent definition json", e);
        }
    }
}
