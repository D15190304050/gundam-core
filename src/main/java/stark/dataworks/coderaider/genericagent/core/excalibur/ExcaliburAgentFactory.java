package stark.dataworks.coderaider.genericagent.core.excalibur;

import stark.dataworks.coderaider.genericagent.core.agent.AgentDefinition;

import java.nio.file.Path;
import java.util.List;

/**
 * Factory for creating Excalibur agents configured for general-purpose software engineering tasks.
 */
public final class ExcaliburAgentFactory
{
    private ExcaliburAgentFactory()
    {
    }

    public static AgentDefinition create(
        String id,
        String model,
        Path workspace,
        ExcaliburAgentRole role,
        String reactInstructions,
        String additionalInstructions)
    {
        ExcaliburTaskRequest taskRequest = ExcaliburTaskRequest.builder(additionalInstructions, workspace).build();
        return createSpec(id, role.getDefaultName(), model, workspace, role, taskRequest, reactInstructions,
            additionalInstructions, role.getDefaultToolNames(), List.of(), true, defaultReasoningEffort(role))
            .toAgentDefinition();
    }

    public static AgentDefinition create(
        String id,
        String name,
        String model,
        Path workspace,
        ExcaliburAgentRole role,
        String reactInstructions,
        String additionalInstructions,
        List<String> toolNames,
        List<String> handoffAgentIds,
        boolean reactEnabled)
    {
        ExcaliburTaskRequest taskRequest = ExcaliburTaskRequest.builder(additionalInstructions, workspace).build();
        return createSpec(id, name, model, workspace, role, taskRequest, reactInstructions, additionalInstructions,
            toolNames, handoffAgentIds, reactEnabled, defaultReasoningEffort(role)).toAgentDefinition();
    }

    public static ExcaliburAgentSpec createSpec(
        String id,
        String name,
        String model,
        Path workspace,
        ExcaliburAgentRole role,
        ExcaliburTaskRequest taskRequest,
        String reactInstructions,
        String additionalInstructions,
        List<String> toolNames,
        List<String> handoffAgentIds,
        boolean reactEnabled,
        String reasoningEffort)
    {
        return ExcaliburAgentSpec.builder(id, model, workspace, role, taskRequest)
            .name(name)
            .reactInstructions(reactInstructions)
            .additionalInstructions(additionalInstructions)
            .toolNames(toolNames)
            .handoffAgentIds(handoffAgentIds)
            .reactEnabled(reactEnabled)
            .reasoningEffort(reasoningEffort)
            .build();
    }

    private static String defaultReasoningEffort(ExcaliburAgentRole role)
    {
        return role == ExcaliburAgentRole.FIXER ? "medium" : "low";
    }
}
