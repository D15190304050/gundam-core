package stark.dataworks.coderaider.agent;

import java.util.Objects;

public class Agent implements IAgent {
    private final AgentDefinition definition;

    public Agent(AgentDefinition definition) {
        definition.validate();
        this.definition = Objects.requireNonNull(definition, "definition");
    }

    @Override
    public AgentDefinition definition() {
        return definition;
    }
}
