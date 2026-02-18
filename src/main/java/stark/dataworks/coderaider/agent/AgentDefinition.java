package stark.dataworks.coderaider.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AgentDefinition {
    private String id;
    private String name;
    private String systemPrompt;
    private String model;
    private List<String> toolNames = new ArrayList<>();
    private List<String> handoffAgentIds = new ArrayList<>();
    private int maxSteps = 8;
    private boolean resetInputAfterToolCall = true;
    private boolean resetInputAfterHandoff = true;
    private boolean requireToolApproval = false;
    private String outputSchemaName;
    private Map<String, Object> metadata = Map.of();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<String> getToolNames() {
        return toolNames;
    }

    public void setToolNames(List<String> toolNames) {
        this.toolNames = toolNames == null ? new ArrayList<>() : toolNames;
    }

    public List<String> getHandoffAgentIds() {
        return handoffAgentIds;
    }

    public void setHandoffAgentIds(List<String> handoffAgentIds) {
        this.handoffAgentIds = handoffAgentIds == null ? new ArrayList<>() : handoffAgentIds;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public boolean isResetInputAfterToolCall() {
        return resetInputAfterToolCall;
    }

    public void setResetInputAfterToolCall(boolean resetInputAfterToolCall) {
        this.resetInputAfterToolCall = resetInputAfterToolCall;
    }

    public boolean isResetInputAfterHandoff() {
        return resetInputAfterHandoff;
    }

    public void setResetInputAfterHandoff(boolean resetInputAfterHandoff) {
        this.resetInputAfterHandoff = resetInputAfterHandoff;
    }

    public boolean isRequireToolApproval() {
        return requireToolApproval;
    }

    public void setRequireToolApproval(boolean requireToolApproval) {
        this.requireToolApproval = requireToolApproval;
    }

    public String getOutputSchemaName() {
        return outputSchemaName;
    }

    public void setOutputSchemaName(String outputSchemaName) {
        this.outputSchemaName = outputSchemaName;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata == null ? Map.of() : metadata;
    }

    public void validate() {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(systemPrompt, "systemPrompt");
        Objects.requireNonNull(model, "model");
        if (maxSteps < 1) {
            throw new IllegalArgumentException("maxSteps must be >= 1");
        }
    }
}
