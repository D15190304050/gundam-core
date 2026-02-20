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
     * Internal state for content; used while coordinating runtime behavior.
     */
    private final String content;

    /**
     * Internal state for tool calls; used while coordinating runtime behavior.
     */
    private final List<ToolCall> toolCalls;

    /**
     * Internal state for handoff agent id; used while coordinating runtime behavior.
     */
    @Getter(AccessLevel.NONE)
    private final String handoffAgentId;

    /**
     * Internal state for token usage; used while coordinating runtime behavior.
     */
    private final TokenUsage tokenUsage;

    /**
     * Internal state for finish reason; used while coordinating runtime behavior.
     */
    private final String finishReason;

    /**
     * Internal state for reasoning content; used while coordinating runtime behavior.
     */
    private final String reasoningContent;

    /**
     * Internal state for structured output; used while coordinating runtime behavior.
     */
    private final Map<String, Object> structuredOutput;

    /**
     * Internal state for generated assets; used while coordinating runtime behavior.
     */
    private final List<GeneratedAsset> generatedAssets;

    /**
     * Performs llm response as part of LlmResponse runtime responsibilities.
     * @param content The content used by this operation.
     * @param toolCalls The tool calls used by this operation.
     * @param handoffAgentId The handoff agent id used by this operation.
     * @param tokenUsage The token usage used by this operation.
     */
    public LlmResponse(String content, List<ToolCall> toolCalls, String handoffAgentId, TokenUsage tokenUsage)
    {
        this(content, toolCalls, handoffAgentId, tokenUsage, "stop", Map.of(), List.of());
    }

    /**
     * Performs llm response as part of LlmResponse runtime responsibilities.
     * @param content The content used by this operation.
     * @param toolCalls The tool calls used by this operation.
     * @param handoffAgentId The handoff agent id used by this operation.
     * @param tokenUsage The token usage used by this operation.
     * @param finishReason The finish reason used by this operation.
     * @param structuredOutput The structured output used by this operation.
     */
    public LlmResponse(String content, List<ToolCall> toolCalls, String handoffAgentId, TokenUsage tokenUsage,
                       String finishReason, Map<String, Object> structuredOutput)
    {
        this(content, toolCalls, handoffAgentId, tokenUsage, finishReason, "", structuredOutput, List.of());
    }

    /**
     * Performs llm response as part of LlmResponse runtime responsibilities.
     * @param content The content used by this operation.
     * @param toolCalls The tool calls used by this operation.
     * @param handoffAgentId The handoff agent id used by this operation.
     * @param tokenUsage The token usage used by this operation.
     * @param finishReason The finish reason used by this operation.
     * @param structuredOutput The structured output used by this operation.
     * @param generatedAssets The generated assets used by this operation.
     */
    public LlmResponse(String content, List<ToolCall> toolCalls, String handoffAgentId, TokenUsage tokenUsage,
                       String finishReason, Map<String, Object> structuredOutput, List<GeneratedAsset> generatedAssets)
    {
        this(content, toolCalls, handoffAgentId, tokenUsage, finishReason, "", structuredOutput, generatedAssets);
    }

    /**
     * Performs llm response as part of LlmResponse runtime responsibilities.
     * @param content The content used by this operation.
     * @param toolCalls The tool calls used by this operation.
     * @param handoffAgentId The handoff agent id used by this operation.
     * @param tokenUsage The token usage used by this operation.
     * @param finishReason The finish reason used by this operation.
     * @param reasoningContent The reasoning content used by this operation.
     * @param structuredOutput The structured output used by this operation.
     * @param generatedAssets The generated assets used by this operation.
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
     * Returns the current handoff agent id value maintained by this LlmResponse.
     * @return The value produced by this operation.
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
