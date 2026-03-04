package stark.dataworks.coderaider.gundam.core.tool.builtin;

import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

import java.util.Map;
import java.util.Objects;

/**
 * AgentTool implements tool contracts, schema metadata, and executable tool registration.
 */
public class AgentTool extends AbstractBuiltinTool
{
    /**
     * Agent runner used to execute nested agent calls.
     */
    private final AgentRunner runner;

    /**
     * Target agent id.
     */
    private final String targetAgentId;

    /**
     * Run configuration applied to delegated agent execution.
     */
    private final RunConfiguration runConfiguration;

    /**
     * Performs agent tool as part of AgentTool runtime responsibilities.
     * @param definition The definition used by this operation.
     * @param runner The runner used by this operation.
     * @param targetAgentId The target agent id used by this operation.
     * @param runConfiguration The run configuration used by this operation.
     */
    public AgentTool(ToolDefinition definition, AgentRunner runner, String targetAgentId, RunConfiguration runConfiguration)
    {
        super(definition, ToolCategory.FUNCTION);
        this.runner = Objects.requireNonNull(runner, "runner");
        this.targetAgentId = Objects.requireNonNull(targetAgentId, "targetAgentId");
        this.runConfiguration = runConfiguration == null ? RunConfiguration.defaults() : runConfiguration;
    }

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param input The input used by this operation.
     * @return The value produced by this operation.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        String task = String.valueOf(input.getOrDefault("task", ""));
        return runner.chatClient(targetAgentId)
            .prompt()
            .user(task)
            .runConfiguration(runConfiguration)
            .runHooks(new stark.dataworks.coderaider.gundam.core.runner.IRunHooks() {})
            .call()
            .content();
    }
}
