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
     * Performs agent as part of Agent runtime responsibilities.
     * @param definition The definition used by this operation.
     */
    public Agent(AgentDefinition definition)
    {
        definition.validate();
        this.definition = Objects.requireNonNull(definition, "definition");
    }

    /**
     * Performs definition as part of Agent runtime responsibilities.
     * @return The value produced by this operation.
     */
    @Override
    public AgentDefinition definition()
    {
        return definition;
    }
}
