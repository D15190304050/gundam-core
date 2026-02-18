package stark.dataworks.coderaider.gundam.core.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AgentDefinition implements agent definitions and lookup used by runners and handoff resolution.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
     * Internal state for model temperature used while coordinating runtime behavior.
     */
    private double modelTemperature = 0.2;

    /**
     * Internal state for max output tokens used while coordinating runtime behavior.
     */
    private int modelMaxTokens = 512;

    /**
     * Internal state for tool choice policy used while coordinating runtime behavior.
     */
    private String modelToolChoice = "auto";

    /**
     * Internal state for response format used while coordinating runtime behavior.
     */
    private String modelResponseFormat = "text";

    /**
     * Internal state for provider model options used while coordinating runtime behavior.
     */
    private Map<String, Object> modelProviderOptions = Map.of();

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

    public void setToolNames(List<String> toolNames)
    {
        this.toolNames = toolNames == null ? new ArrayList<>() : toolNames;
    }

    public void setHandoffAgentIds(List<String> handoffAgentIds)
    {
        this.handoffAgentIds = handoffAgentIds == null ? new ArrayList<>() : handoffAgentIds;
    }

    public void setMetadata(Map<String, Object> metadata)
    {
        this.metadata = metadata == null ? Map.of() : metadata;
    }

    public void setModelToolChoice(String modelToolChoice)
    {
        this.modelToolChoice = modelToolChoice == null ? "auto" : modelToolChoice;
    }

    public void setModelResponseFormat(String modelResponseFormat)
    {
        this.modelResponseFormat = modelResponseFormat == null ? "text" : modelResponseFormat;
    }

    public void setModelProviderOptions(Map<String, Object> modelProviderOptions)
    {
        this.modelProviderOptions = modelProviderOptions == null ? Map.of() : modelProviderOptions;
    }

    /**
     * Validates and throws when required constraints are violated.
     */
    public void validate()
    {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(systemPrompt, "systemPrompt");
        Objects.requireNonNull(model, "model");
        if (modelTemperature < 0 || modelTemperature > 2)
        {
            throw new IllegalArgumentException("modelTemperature must be between 0 and 2");
        }
        if (modelMaxTokens < 1)
        {
            throw new IllegalArgumentException("modelMaxTokens must be >= 1");
        }
        if (maxSteps < 1)
        {
            throw new IllegalArgumentException("maxSteps must be >= 1");
        }
    }
}
