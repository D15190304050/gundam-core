package stark.dataworks.coderaider.genericagent.core.excalibur;

import stark.dataworks.coderaider.genericagent.core.agent.AgentDefinition;

import java.nio.file.Path;
import java.util.List;

/**
 * The {@link ExcaliburAgentFactory} class provides static methods to create an instance of Excalibur agent.
 */
public final class ExcaliburAgentFactory
{
    public static AgentDefinition create(
        String id,
        String name,
        String model,
        Path workspace,
        String reactInstructions,
        String additionalInstructions,
        List<String> toolNames)
    {
        ExcaliburTaskRequest taskRequest = ExcaliburTaskRequest.builder(additionalInstructions, workspace).build();
        return createSpec(id, name, model, workspace, taskRequest, reactInstructions,
            additionalInstructions, toolNames, List.of(), true, "low").toAgentDefinition();
    }

    public static ExcaliburAgentSpec createSpec(
        String id,
        String name,
        String model,
        Path workspace,
        ExcaliburTaskRequest taskRequest,
        String reactInstructions,
        String additionalInstructions,
        List<String> toolNames,
        List<String> handoffAgentIds,
        boolean reactEnabled,
        String reasoningEffort)
    {
        return ExcaliburAgentSpec.builder(id, model, workspace, name, toolNames, taskRequest)
            .reactInstructions(reactInstructions)
            .additionalInstructions(additionalInstructions)
            .handoffAgentIds(handoffAgentIds)
            .reactEnabled(reactEnabled)
            .reasoningEffort(reasoningEffort)
            .build();
    }
}