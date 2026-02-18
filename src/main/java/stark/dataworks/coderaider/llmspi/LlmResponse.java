package stark.dataworks.coderaider.llmspi;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import stark.dataworks.coderaider.metrics.TokenUsage;
import stark.dataworks.coderaider.model.ToolCall;

public class LlmResponse {
    private final String content;
    private final List<ToolCall> toolCalls;
    private final String handoffAgentId;
    private final TokenUsage tokenUsage;
    private final String finishReason;
    private final Map<String, Object> structuredOutput;

    public LlmResponse(String content, List<ToolCall> toolCalls, String handoffAgentId, TokenUsage tokenUsage) {
        this(content, toolCalls, handoffAgentId, tokenUsage, "stop", Map.of());
    }

    public LlmResponse(String content,
                       List<ToolCall> toolCalls,
                       String handoffAgentId,
                       TokenUsage tokenUsage,
                       String finishReason,
                       Map<String, Object> structuredOutput) {
        this.content = content == null ? "" : content;
        this.toolCalls = Collections.unmodifiableList(Objects.requireNonNull(toolCalls, "toolCalls"));
        this.handoffAgentId = handoffAgentId;
        this.tokenUsage = Objects.requireNonNull(tokenUsage, "tokenUsage");
        this.finishReason = finishReason == null ? "stop" : finishReason;
        this.structuredOutput = Collections.unmodifiableMap(structuredOutput == null ? Map.of() : structuredOutput);
    }

    public String getContent() {
        return content;
    }

    public List<ToolCall> getToolCalls() {
        return toolCalls;
    }

    public Optional<String> getHandoffAgentId() {
        return Optional.ofNullable(handoffAgentId);
    }

    public TokenUsage getTokenUsage() {
        return tokenUsage;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public Map<String, Object> getStructuredOutput() {
        return structuredOutput;
    }

    public boolean isFinal() {
        return toolCalls.isEmpty() && handoffAgentId == null;
    }
}
