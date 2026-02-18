package stark.dataworks.coderaider.gundam.core.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
/**
 * Class AgentDefinition.
 */

public class AgentDefinition
{
    /**
     * Field id.
     */
    private String id;
    /**
     * Field name.
     */
    private String name;
    /**
     * Field systemPrompt.
     */
    private String systemPrompt;
    /**
     * Field model.
     */
    private String model;
    /**
     * Field toolNames.
     */
    private List<String> toolNames = new ArrayList<>();
    /**
     * Field handoffAgentIds.
     */
    private List<String> handoffAgentIds = new ArrayList<>();
    /**
     * Field maxSteps.
     */
    private int maxSteps = 8;
    /**
     * Field resetInputAfterToolCall.
     */
    private boolean resetInputAfterToolCall = true;
    /**
     * Field resetInputAfterHandoff.
     */
    private boolean resetInputAfterHandoff = true;
    /**
     * Field requireToolApproval.
     */
    private boolean requireToolApproval = false;
    /**
     * Field outputSchemaName.
     */
    private String outputSchemaName;
    /**
     * Field metadata.
     */
    private Map<String, Object> metadata = Map.of();
    /**
     * Executes getId.
     */

    public String getId()
    {
        return id;
    }
    /**
     * Executes setId.
     */

    public void setId(String id)
    {
        this.id = id;
    }
    /**
     * Executes getName.
     */

    public String getName()
    {
        return name;
    }
    /**
     * Executes setName.
     */

    public void setName(String name)
    {
        this.name = name;
    }
    /**
     * Executes getSystemPrompt.
     */

    public String getSystemPrompt()
    {
        return systemPrompt;
    }
    /**
     * Executes setSystemPrompt.
     */

    public void setSystemPrompt(String systemPrompt)
    {
        this.systemPrompt = systemPrompt;
    }
    /**
     * Executes getModel.
     */

    public String getModel()
    {
        return model;
    }
    /**
     * Executes setModel.
     */

    public void setModel(String model)
    {
        this.model = model;
    }
    /**
     * Executes getToolNames.
     */

    public List<String> getToolNames()
    {
        return toolNames;
    }
    /**
     * Executes setToolNames.
     */

    public void setToolNames(List<String> toolNames)
    {
        this.toolNames = toolNames == null ? new ArrayList<>() : toolNames;
    }
    /**
     * Executes getHandoffAgentIds.
     */

    public List<String> getHandoffAgentIds()
    {
        return handoffAgentIds;
    }
    /**
     * Executes setHandoffAgentIds.
     */

    public void setHandoffAgentIds(List<String> handoffAgentIds)
    {
        this.handoffAgentIds = handoffAgentIds == null ? new ArrayList<>() : handoffAgentIds;
    }
    /**
     * Executes getMaxSteps.
     */

    public int getMaxSteps()
    {
        return maxSteps;
    }
    /**
     * Executes setMaxSteps.
     */

    public void setMaxSteps(int maxSteps)
    {
        this.maxSteps = maxSteps;
    }
    /**
     * Executes isResetInputAfterToolCall.
     */

    public boolean isResetInputAfterToolCall()
    {
        return resetInputAfterToolCall;
    }
    /**
     * Executes setResetInputAfterToolCall.
     */

    public void setResetInputAfterToolCall(boolean resetInputAfterToolCall)
    {
        this.resetInputAfterToolCall = resetInputAfterToolCall;
    }
    /**
     * Executes isResetInputAfterHandoff.
     */

    public boolean isResetInputAfterHandoff()
    {
        return resetInputAfterHandoff;
    }
    /**
     * Executes setResetInputAfterHandoff.
     */

    public void setResetInputAfterHandoff(boolean resetInputAfterHandoff)
    {
        this.resetInputAfterHandoff = resetInputAfterHandoff;
    }
    /**
     * Executes isRequireToolApproval.
     */

    public boolean isRequireToolApproval()
    {
        return requireToolApproval;
    }
    /**
     * Executes setRequireToolApproval.
     */

    public void setRequireToolApproval(boolean requireToolApproval)
    {
        this.requireToolApproval = requireToolApproval;
    }
    /**
     * Executes getOutputSchemaName.
     */

    public String getOutputSchemaName()
    {
        return outputSchemaName;
    }
    /**
     * Executes setOutputSchemaName.
     */

    public void setOutputSchemaName(String outputSchemaName)
    {
        this.outputSchemaName = outputSchemaName;
    }
    /**
     * Executes getMetadata.
     */

    public Map<String, Object> getMetadata()
    {
        return metadata;
    }
    /**
     * Executes setMetadata.
     */

    public void setMetadata(Map<String, Object> metadata)
    {
        this.metadata = metadata == null ? Map.of() : metadata;
    }
    /**
     * Executes validate.
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
