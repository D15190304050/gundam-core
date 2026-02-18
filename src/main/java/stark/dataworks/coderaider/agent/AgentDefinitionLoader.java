package stark.dataworks.coderaider.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class AgentDefinitionLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AgentDefinitionLoader() {
    }

    public static AgentDefinition fromJson(String json) {
        try {
            AgentDefinition definition = MAPPER.readValue(json, AgentDefinition.class);
            definition.validate();
            return definition;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid agent definition json", e);
        }
    }
}
