package stark.dataworks.coderaider.gundam.core.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AgentDefinition implements agent definitions and lookup used by runners and handoff resolution.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class AgentDefinition
{

    /**
     * Internal state for id; used while coordinating runtime behavior.
     */
    private String id;

    /**
     * Internal state for name; used while coordinating runtime behavior.
     */
    private String name;

    /**
     * Internal state for system prompt; used while coordinating runtime behavior.
     */
    private String systemPrompt;

    /**
     * Internal state for model; used while coordinating runtime behavior.
     */
    private String model;

    /**
     * Internal state for tool names used while coordinating runtime behavior.
     */
    private List<String> toolNames = new ArrayList<>();

    /**
     * Internal state for handoff agent ids used while coordinating runtime behavior.
     */
    private List<String> handoffAgentIds = new ArrayList<>();

    /**
     * Internal state for max steps used while coordinating runtime behavior.
     */
    private int maxSteps = 8;

    /**
     * Internal state for reset input after tool call used while coordinating runtime behavior.
     */
    private boolean resetInputAfterToolCall = true;

    /**
     * Internal state for reset input after handoff used while coordinating runtime behavior.
     */
    private boolean resetInputAfterHandoff = true;

    /**
     * Internal state for require tool approval used while coordinating runtime behavior.
     */
    private boolean requireToolApproval = false;

    /**
     * Internal state for output schema name; used while coordinating runtime behavior.
     */
    private String outputSchemaName;

    /**
     * Internal state for metadata used while coordinating runtime behavior.
     */
    private Map<String, Object> metadata = Map.of();

    /**
     * Returns the current id value maintained by this AgentDefinition.
     * @return The value produced by this operation.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Updates the id value used by this AgentDefinition for later operations.
     * @param id The id used by this operation.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Returns the current name value maintained by this AgentDefinition.
     * @return The value produced by this operation.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Updates the name value used by this AgentDefinition for later operations.
     * @param name The name used by this operation.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the current system prompt value maintained by this AgentDefinition.
     * @return The value produced by this operation.
     */
    public String getSystemPrompt()
    {
        return systemPrompt;
    }

    /**
     * Updates the system prompt value used by this AgentDefinition for later operations.
     * @param systemPrompt The system prompt used by this operation.
     */
    public void setSystemPrompt(String systemPrompt)
    {
        this.systemPrompt = systemPrompt;
    }

    /**
     * Returns the current model value maintained by this AgentDefinition.
     * @return The value produced by this operation.
     */
    public String getModel()
    {
        return model;
    }

    /**
     * Updates the model value used by this AgentDefinition for later operations.
     * @param model The model used by this operation.
     */
    public void setModel(String model)
    {
        this.model = model;
    }

    /**
     * Returns the current tool names value maintained by this AgentDefinition.
     * @return The value produced by this operation.
     */
    public List<String> getToolNames()
    {
        return toolNames;
    }

    /**
     * Updates the tool names value used by this AgentDefinition for later operations.
     * @param toolNames The tool names used by this operation.
     */
    public void setToolNames(List<String> toolNames)
    {
        this.toolNames = toolNames == null ? new ArrayList<>() : toolNames;
    }

    /**
     * Returns the current handoff agent ids value maintained by this AgentDefinition.
     * @return The value produced by this operation.
     */
    public List<String> getHandoffAgentIds()
    {
        return handoffAgentIds;
    }

    /**
     * Updates the handoff agent ids value used by this AgentDefinition for later operations.
     * @param handoffAgentIds The handoff agent ids used by this operation.
     */
    public void setHandoffAgentIds(List<String> handoffAgentIds)
    {
        this.handoffAgentIds = handoffAgentIds == null ? new ArrayList<>() : handoffAgentIds;
    }

    /**
     * Returns the current max steps value maintained by this AgentDefinition.
     * @return The value produced by this operation.
     */
    public int getMaxSteps()
    {
        return maxSteps;
    }

    /**
     * Updates the max steps value used by this AgentDefinition for later operations.
     * @param maxSteps The max steps used by this operation.
     */
    public void setMaxSteps(int maxSteps)
    {
        this.maxSteps = maxSteps;
    }

    /**
     * Reports whether reset input after tool call is currently satisfied.
     * @return {@code true} when the condition is satisfied; otherwise {@code false}.
     */
    public boolean isResetInputAfterToolCall()
    {
        return resetInputAfterToolCall;
    }

    /**
     * Updates the reset input after tool call value used by this AgentDefinition for later operations.
     * @param resetInputAfterToolCall The reset input after tool call used by this operation.
     */
    public void setResetInputAfterToolCall(boolean resetInputAfterToolCall)
    {
        this.resetInputAfterToolCall = resetInputAfterToolCall;
    }

    /**
     * Reports whether reset input after handoff is currently satisfied.
     * @return {@code true} when the condition is satisfied; otherwise {@code false}.
     */
    public boolean isResetInputAfterHandoff()
    {
        return resetInputAfterHandoff;
    }

    /**
     * Updates the reset input after handoff value used by this AgentDefinition for later operations.
     * @param resetInputAfterHandoff The reset input after handoff used by this operation.
     */
    public void setResetInputAfterHandoff(boolean resetInputAfterHandoff)
    {
        this.resetInputAfterHandoff = resetInputAfterHandoff;
    }

    /**
     * Reports whether require tool approval is currently satisfied.
     * @return {@code true} when the condition is satisfied; otherwise {@code false}.
     */
    public boolean isRequireToolApproval()
    {
        return requireToolApproval;
    }

    /**
     * Updates the require tool approval value used by this AgentDefinition for later operations.
     * @param requireToolApproval The require tool approval used by this operation.
     */
    public void setRequireToolApproval(boolean requireToolApproval)
    {
        this.requireToolApproval = requireToolApproval;
    }

    /**
     * Returns the current output schema name value maintained by this AgentDefinition.
     * @return The value produced by this operation.
     */
    public String getOutputSchemaName()
    {
        return outputSchemaName;
    }

    /**
     * Updates the output schema name value used by this AgentDefinition for later operations.
     * @param outputSchemaName The output schema name used by this operation.
     */
    public void setOutputSchemaName(String outputSchemaName)
    {
        this.outputSchemaName = outputSchemaName;
    }

    /**
     * Returns the current metadata value maintained by this AgentDefinition.
     * @return The value produced by this operation.
     */
    public Map<String, Object> getMetadata()
    {
        return metadata;
    }

    /**
     * Updates the metadata value used by this AgentDefinition for later operations.
     * @param metadata The metadata used by this operation.
     */
    public void setMetadata(Map<String, Object> metadata)
    {
        this.metadata = metadata == null ? Map.of() : metadata;
    }

    /**
     * Validates  and throws when required constraints are violated.
     */
    public void validate()
    {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(systemPrompt, "systemPrompt");
        Objects.requireNonNull(model, "model");
        if (maxSteps < 1)
        {
            throw new IllegalArgumentException("maxSteps must be >= 1");
        }
    }
}
