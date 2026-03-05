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
     * Initializes AgentTool with required runtime dependencies and options.
     * @param definition definition object.
     * @param runner agent runner.
     * @param targetAgentId target agent id.
     * @param runConfiguration run configuration.
     */
    public AgentTool(ToolDefinition definition, AgentRunner runner, String targetAgentId, RunConfiguration runConfiguration)
    {
        super(definition, ToolCategory.FUNCTION);
        this.runner = Objects.requireNonNull(runner, "runner");
        this.targetAgentId = Objects.requireNonNull(targetAgentId, "targetAgentId");
        this.runConfiguration = runConfiguration == null ? RunConfiguration.defaults() : runConfiguration;
    }

    /**
     * Executes this tool operation and returns the produced output.
     * @param Map<String map<string.
     * @param input input payload.
     * @return Tool execution output returned by the MCP server.
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
