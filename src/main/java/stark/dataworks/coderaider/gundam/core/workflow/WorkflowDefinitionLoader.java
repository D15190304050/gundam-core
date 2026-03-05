package stark.dataworks.coderaider.gundam.core.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * WorkflowDefinitionLoader implements workflow DAG json loading.
 */
public final class WorkflowDefinitionLoader
{
    /**
     * ObjectMapper used for JSON serialization/deserialization.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Initializes WorkflowDefinitionLoader with required runtime dependencies and options.
     */
    private WorkflowDefinitionLoader()
    {
    }

    /**
     * Serializes the value to JSON.
     * @param json JSON document to parse.
     * @return workflow definition result.
     */
    public static WorkflowDefinition fromJson(String json)
    {
        try
        {
            WorkflowDefinition definition = MAPPER.readValue(json, WorkflowDefinition.class);
            definition.validate();
            return definition;
        }
        catch (JsonProcessingException e)
        {
            throw new IllegalArgumentException("Invalid workflow definition json", e);
        }
    }
}
