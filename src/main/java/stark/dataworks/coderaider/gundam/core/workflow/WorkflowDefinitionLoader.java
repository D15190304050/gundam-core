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
     * Performs workflow definition loader as part of WorkflowDefinitionLoader runtime responsibilities.
     */
    private WorkflowDefinitionLoader()
    {
    }

    /**
     * Performs from json as part of WorkflowDefinitionLoader runtime responsibilities.
     * @param json The json used by this operation.
     * @return The value produced by this operation.
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
