package stark.dataworks.coderaider.gundam.core.llmspi;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
import stark.dataworks.coderaider.gundam.core.multimodal.GeneratedAsset;

/**
 * LlmResponse implements provider-agnostic model invocation contracts.
 */
@Getter
public class LlmResponse
{
    /**
     * Main assistant text content returned by the model.
     */
    private final String content;

    /**
     * Tool calls emitted by the assistant response.
     */
    private final List<ToolCall> toolCalls;

    /**
     * Agent id selected by the model for handoff.
     */
    @Getter(AccessLevel.NONE)
    private final String handoffAgentId;

    /**
     * Token accounting data used for cost and quota tracking.
     */
    private final TokenUsage tokenUsage;

    /**
     * Provider-reported reason why generation stopped.
     */
    private final String finishReason;

    /**
     * Reasoning text/deltas returned by reasoning-capable providers.
     */
    private final String reasoningContent;

    /**
     * Parsed structured output payload returned by the model.
     */
    private final Map<String, Object> structuredOutput;

    /**
     * Generated asset metadata (image/audio/video urls, etc.).
     */
    private final List<GeneratedAsset> generatedAssets;

    /**
     * Initializes LlmResponse with required runtime dependencies and options.
     * @param content content.
     * @param toolCalls tool calls.
     * @param handoffAgentId handoff agent id.
     * @param tokenUsage token usage.
     */
    public LlmResponse(String content, List<ToolCall> toolCalls, String handoffAgentId, TokenUsage tokenUsage)
    {
        this(content, toolCalls, handoffAgentId, tokenUsage, "stop", Map.of(), List.of());
    }

    /**
     * Creates an LLM response.
     * @param content content.
     * @param toolCalls tool calls.
     * @param handoffAgentId handoff agent identifier.
     * @param tokenUsage token-usage data.
     * @param finishReason finish reason.
     * @param structuredOutput structured output payload.
     */
    public LlmResponse(String content, List<ToolCall> toolCalls, String handoffAgentId, TokenUsage tokenUsage,
                       String finishReason, Map<String, Object> structuredOutput)
    {
        this(content, toolCalls, handoffAgentId, tokenUsage, finishReason, "", structuredOutput, List.of());
    }

    /**
     * Creates an LLM response.
     * @param content content.
     * @param toolCalls tool calls.
     * @param handoffAgentId handoff agent identifier.
     * @param tokenUsage token-usage data.
     * @param finishReason finish reason.
     * @param structuredOutput structured output payload.
     * @param generatedAssets generated asset metadata.
     */
    public LlmResponse(String content, List<ToolCall> toolCalls, String handoffAgentId, TokenUsage tokenUsage,
                       String finishReason, Map<String, Object> structuredOutput, List<GeneratedAsset> generatedAssets)
    {
        this(content, toolCalls, handoffAgentId, tokenUsage, finishReason, "", structuredOutput, generatedAssets);
    }

    /**
     * Creates an LLM response.
     * @param content content.
     * @param toolCalls tool calls.
     * @param handoffAgentId handoff agent identifier.
     * @param tokenUsage token-usage data.
     * @param finishReason finish reason.
     * @param reasoningContent reasoning content.
     * @param structuredOutput structured output payload.
     * @param generatedAssets generated asset metadata.
     */
    public LlmResponse(String content, List<ToolCall> toolCalls, String handoffAgentId, TokenUsage tokenUsage,
                       String finishReason, String reasoningContent, Map<String, Object> structuredOutput,
                       List<GeneratedAsset> generatedAssets)
    {
        this.content = content == null ? "" : content;
        this.toolCalls = Collections.unmodifiableList(toolCalls == null ? List.of() : toolCalls);
        this.handoffAgentId = handoffAgentId;
        this.tokenUsage = tokenUsage;
        this.finishReason = finishReason == null ? "" : finishReason;
        this.reasoningContent = reasoningContent == null ? "" : reasoningContent;
        this.structuredOutput = Collections.unmodifiableMap(structuredOutput == null ? Map.of() : structuredOutput);
        this.generatedAssets = Collections.unmodifiableList(generatedAssets == null ? List.of() : generatedAssets);
    }

    /**
     * Returns handoff agent id.
     * @return Optional string value.
     */
    public Optional<String> getHandoffAgentId()
    {
        return Optional.ofNullable(handoffAgentId);
    }

    /**
     * Reports whether final is currently satisfied.
     * @return {@code true} when the condition is satisfied; otherwise {@code false}.
     */
    public boolean isFinal()
    {
        return toolCalls.isEmpty() && handoffAgentId == null;
    }
}
