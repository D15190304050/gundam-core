package stark.dataworks.coderaider.gundam.core.agent;

import java.util.Objects;

/**
 * Agent implements agent definitions and lookup used by runners and handoff resolution.
 */
public class Agent implements IAgent
{

    /**
     * Immutable definition object that configures this runtime instance.
     */
    private final AgentDefinition definition;

    /**
     * Initializes Agent with required runtime dependencies and options.
     * @param definition definition object.
     */
    public Agent(AgentDefinition definition)
    {
        definition.validate();
        this.definition = Objects.requireNonNull(definition, "definition");
    }

    /**
     * Returns definition metadata for this component.
     * @return Agent definition associated with this instance.
     */
    @Override
    public AgentDefinition definition()
    {
        return definition;
    }
}
