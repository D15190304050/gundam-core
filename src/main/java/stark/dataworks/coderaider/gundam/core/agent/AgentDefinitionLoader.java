package stark.dataworks.coderaider.gundam.core.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * Class AgentDefinitionLoader.
 */

public final class AgentDefinitionLoader
{
    /**
     * Field MAPPER.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();
    /**
     * Creates a new AgentDefinitionLoader instance.
     */

    private AgentDefinitionLoader()
    {
    }
    /**
     * Executes fromJson.
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
