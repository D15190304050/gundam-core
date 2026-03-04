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
     * Performs agent definition loader as part of AgentDefinitionLoader runtime responsibilities.
     */
    private AgentDefinitionLoader()
    {
    }

    /**
     * Performs from json as part of AgentDefinitionLoader runtime responsibilities.
     * @param json The json used by this operation.
     * @return The value produced by this operation.
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
