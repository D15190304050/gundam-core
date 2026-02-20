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
     * Performs on delta as part of LlmStreamListener runtime responsibilities.
     * @param delta The delta used by this operation.
     */
    void onDelta(String delta);

    /**
     * Performs on tool call as part of LlmStreamListener runtime responsibilities.
     * @param toolCall The tool call used by this operation.
     */
    default void onToolCall(ToolCall toolCall)
    {
    }

    /**
     * Performs on reasoning delta as part of LlmStreamListener runtime responsibilities.
     * @param reasoningDelta The reasoning delta used by this operation.
     */
    default void onReasoningDelta(String reasoningDelta)
    {
    }

    /**
     * Performs on token usage as part of LlmStreamListener runtime responsibilities.
     * @param tokenUsage The token usage used by this operation.
     */
    default void onTokenUsage(TokenUsage tokenUsage)
    {
    }

    /**
     * Performs on handoff as part of LlmStreamListener runtime responsibilities.
     * @param handoffAgentId The handoff agent id used by this operation.
     */
    default void onHandoff(String handoffAgentId)
    {
    }

    /**
     * Performs on completed as part of LlmStreamListener runtime responsibilities.
     * @param response The response used by this operation.
     */
    default void onCompleted(LlmResponse response)
    {
    }
}
