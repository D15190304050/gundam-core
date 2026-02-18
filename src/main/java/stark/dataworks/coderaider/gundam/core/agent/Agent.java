package stark.dataworks.coderaider.gundam.core.agent;

import java.util.Objects;
/**
 * Class Agent.
 */

public class Agent implements IAgent
{
    /**
     * Field definition.
     */
    private final AgentDefinition definition;
    /**
     * Creates a new Agent instance.
     */

    public Agent(AgentDefinition definition)
    {
        definition.validate();
        this.definition = Objects.requireNonNull(definition, "definition");
    }

    /**
     * Executes definition.
     */
    @Override
    public AgentDefinition definition()
    {
        return definition;
    }
}
