package stark.dataworks.coderaider.gundam.core.llmspi;

import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;

/**
 * LlmStreamListener implements provider-agnostic model invocation contracts.
 */
@FunctionalInterface
public interface ILlmStreamListener
{

    /**
     * Invoked for streamed response text deltas.
     * @param delta delta.
     */
    void onDelta(String delta);

    /**
     * Invoked when a streamed tool call is produced.
     * @param toolCall tool call payload.
     */
    default void onToolCall(ToolCall toolCall)
    {
    }

    /**
     * Invoked for streamed reasoning content.
     * @param reasoningDelta reasoning delta.
     */
    default void onReasoningDelta(String reasoningDelta)
    {
    }

    /**
     * Invoked when token usage is reported.
     * @param tokenUsage token usage.
     */
    default void onTokenUsage(TokenUsage tokenUsage)
    {
    }

    /**
     * Invoked when the model requests agent handoff.
     * @param handoffAgentId handoff agent id.
     */
    default void onHandoff(String handoffAgentId)
    {
    }

    /**
     * Invoked when streaming completes.
     * @param response model/tool response payload.
     */
    default void onCompleted(LlmResponse response)
    {
    }
}
