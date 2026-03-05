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
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDefinition
{

    /**
     * Unique identifier for this definition.
     */
    private String id;

    /**
     * Human-readable name used in logs and UIs.
     */
    private String name;

    /**
     * Base instruction prompt prepended to each model call.
     */
    private String systemPrompt;

    /**
     * Model id passed to the provider for inference.
     */
    private String model;

    /**
 * Sampling temperature used for model generation.
     */
    private double modelTemperature = 0.2;

    /**
 * Maximum output tokens allowed for this configuration.
     */
    private int modelMaxTokens = 512;

    /**
 * Tool selection policy sent to the model.
     */
    private String modelToolChoice = "auto";

    /**
 * Requested response format for model output.
     */
    private String modelResponseFormat = "text";

    /**
 * Provider-specific options forwarded as-is with model requests.
     */
    private Map<String, Object> modelProviderOptions = Map.of();


    /**
 * Reasoning options for providers that support explicit reasoning controls.
     */
    private Map<String, Object> modelReasoning = Map.of();

    /**
 * Skill descriptors exposed to the model runtime.
     */
    private List<Map<String, Object>> modelSkills = new ArrayList<>();

    /**
 * Tools this agent is allowed to call.
     */
    private List<String> toolNames = new ArrayList<>();

    /**
 * Ordered list of handoff agent ids used to resolve cross-component references.
     */
    private List<String> handoffAgentIds = new ArrayList<>();

    /**
 * Maximum steps allowed for this configuration.
     */
    private int maxSteps = 8;

    /**
 * Whether to reset input after tool call to avoid carrying stale context into the next turn.
     */
    private boolean resetInputAfterToolCall = true;

    /**
 * Whether to reset input after handoff to avoid carrying stale context into the next turn.
     */
    private boolean resetInputAfterHandoff = true;

    /**
 * Whether tool calls must be approved before execution.
     */
    private boolean requireToolApproval = false;

    /**
     * Internal state controlling ReAct mode prompt augmentation.
     */
    private boolean reactEnabled = false;

    /**
     * Additional ReAct instructions appended to the system prompt before each run.
     */
    private String reactInstructions;

    /**
     * Output schema name.
     */
    private String outputSchemaName;

    /**
 * Arbitrary metadata attached for caller-specific routing or auditing.
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

    public void setModelReasoning(Map<String, Object> modelReasoning)
    {
        this.modelReasoning = modelReasoning == null ? Map.of() : modelReasoning;
    }

    public void setModelSkills(List<Map<String, Object>> modelSkills)
    {
        this.modelSkills = modelSkills == null ? new ArrayList<>() : modelSkills;
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
