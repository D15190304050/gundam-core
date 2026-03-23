package stark.dataworks.coderaider.genericagent.core.excalibur;

import stark.dataworks.coderaider.genericagent.core.agent.AgentDefinition;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Workspace-aware Excalibur agent spec that bundles the prompt, task bootstrap message, and patch semantics.
 */
public final class ExcaliburAgentSpec
{
    private final AgentDefinition definition;
    private final ExcaliburTaskRequest taskRequest;

    private ExcaliburAgentSpec(Builder builder)
    {
        this.taskRequest = Objects.requireNonNull(builder.taskRequest, "taskRequest");
        AgentDefinition agentDefinition = new AgentDefinition();
        agentDefinition.setId(Objects.requireNonNull(builder.id, "id"));
        agentDefinition.setName(builder.name == null ? builder.role.getDefaultName() : builder.name);
        agentDefinition.setModel(Objects.requireNonNull(builder.model, "model"));
        agentDefinition.setReactEnabled(builder.reactEnabled);
        agentDefinition.setSystemPrompt(ExcaliburSystemPrompts.build(builder.workspace, builder.role,
            ExcaliburTaskMessages.buildRoleSpecificInstructions(taskRequest, builder.role)
                + "\n\n"
                + (builder.additionalInstructions == null ? "" : builder.additionalInstructions)));
        agentDefinition.setReactInstructions(builder.reactInstructions);
        agentDefinition.setToolNames(builder.toolNames == null ? builder.role.getDefaultToolNames() : builder.toolNames);
        agentDefinition.setHandoffAgentIds(builder.handoffAgentIds == null ? List.of() : builder.handoffAgentIds);
        agentDefinition.setModelProviderOptions(Map.of("working_directory", builder.workspace.toAbsolutePath().normalize().toString()));
        agentDefinition.setModelReasoning(Map.of("effort", builder.reasoningEffort));
        this.definition = agentDefinition;
    }

    public AgentDefinition toAgentDefinition()
    {
        return definition;
    }

    public String initialUserMessage()
    {
        return ExcaliburTaskMessages.buildInitialUserMessage(taskRequest);
    }

    public boolean hasRequiredPatch()
    {
        return ExcaliburPatchUtils.hasRequiredPatch(taskRequest);
    }

    public String taskIncompleteMessage()
    {
        return ExcaliburPatchUtils.taskIncompleteMessage();
    }

    public String currentGitDiff()
    {
        return ExcaliburPatchUtils.getGitDiff(taskRequest.getProjectPath(), taskRequest.getBaseCommit());
    }

    public static Builder builder(String id, String model, Path workspace, ExcaliburAgentRole role, ExcaliburTaskRequest taskRequest)
    {
        return new Builder(id, model, workspace, role, taskRequest);
    }

    public static final class Builder
    {
        private final String id;
        private final String model;
        private final Path workspace;
        private final ExcaliburAgentRole role;
        private final ExcaliburTaskRequest taskRequest;
        private String name;
        private String reactInstructions;
        private String additionalInstructions = "";
        private List<String> toolNames;
        private List<String> handoffAgentIds;
        private boolean reactEnabled = true;
        private String reasoningEffort = "low";

        private Builder(String id, String model, Path workspace, ExcaliburAgentRole role, ExcaliburTaskRequest taskRequest)
        {
            this.id = id;
            this.model = model;
            this.workspace = workspace.toAbsolutePath().normalize();
            this.role = role;
            this.taskRequest = taskRequest;
            if (role == ExcaliburAgentRole.FIXER)
            {
                this.reasoningEffort = "medium";
            }
        }

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public Builder reactInstructions(String reactInstructions)
        {
            this.reactInstructions = reactInstructions;
            return this;
        }

        public Builder additionalInstructions(String additionalInstructions)
        {
            this.additionalInstructions = additionalInstructions;
            return this;
        }

        public Builder toolNames(List<String> toolNames)
        {
            this.toolNames = toolNames;
            return this;
        }

        public Builder handoffAgentIds(List<String> handoffAgentIds)
        {
            this.handoffAgentIds = handoffAgentIds;
            return this;
        }

        public Builder reactEnabled(boolean reactEnabled)
        {
            this.reactEnabled = reactEnabled;
            return this;
        }

        public Builder reasoningEffort(String reasoningEffort)
        {
            this.reasoningEffort = reasoningEffort;
            return this;
        }

        public ExcaliburAgentSpec build()
        {
            return new ExcaliburAgentSpec(this);
        }
    }
}
