package stark.dataworks.coderaider.gundam.core.llmspi;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
/**
 * Class LlmResponse.
 */

public class LlmResponse
{
    /**
     * Field content.
     */
    private final String content;
    /**
     * Field toolCalls.
     */
    private final List<ToolCall> toolCalls;
    /**
     * Field handoffAgentId.
     */
    private final String handoffAgentId;
    /**
     * Field tokenUsage.
     */
    private final TokenUsage tokenUsage;
    /**
     * Field finishReason.
     */
    private final String finishReason;
    /**
     * Field structuredOutput.
     */
    private final Map<String, Object> structuredOutput;
    /**
     * Creates a new LlmResponse instance.
     */

    public LlmResponse(String content, List<ToolCall> toolCalls, String handoffAgentId, TokenUsage tokenUsage)
    {
        this(content, toolCalls, handoffAgentId, tokenUsage, "stop", Map.of());
    }
    /**
     * Creates a new LlmResponse instance.
     */

    public LlmResponse(String content,
                       List<ToolCall> toolCalls,
                       String handoffAgentId,
                       TokenUsage tokenUsage,
                       String finishReason,
                       Map<String, Object> structuredOutput)
    {
        this.content = content == null ? "" : content;
        this.toolCalls = Collections.unmodifiableList(Objects.requireNonNull(toolCalls, "toolCalls"));
        this.handoffAgentId = handoffAgentId;
        this.tokenUsage = Objects.requireNonNull(tokenUsage, "tokenUsage");
        this.finishReason = finishReason == null ? "stop" : finishReason;
        this.structuredOutput = Collections.unmodifiableMap(structuredOutput == null ? Map.of() : structuredOutput);
    }
    /**
     * Executes getContent.
     */

    public String getContent()
    {
        return content;
    }
    /**
     * Executes getToolCalls.
     */

    public List<ToolCall> getToolCalls()
    {
        return toolCalls;
    }
    /**
     * Executes getHandoffAgentId.
     */

    public Optional<String> getHandoffAgentId()
    {
        return Optional.ofNullable(handoffAgentId);
    }
    /**
     * Executes getTokenUsage.
     */

    public TokenUsage getTokenUsage()
    {
        return tokenUsage;
    }
    /**
     * Executes getFinishReason.
     */

    public String getFinishReason()
    {
        return finishReason;
    }
    /**
     * Executes getStructuredOutput.
     */

    public Map<String, Object> getStructuredOutput()
    {
        return structuredOutput;
    }
    /**
     * Executes isFinal.
     */

    public boolean isFinal()
    {
        return toolCalls.isEmpty() && handoffAgentId == null;
    }
}
