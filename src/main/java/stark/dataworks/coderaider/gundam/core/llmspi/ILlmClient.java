package stark.dataworks.coderaider.gundam.core.llmspi;

/**
 * ILlmClient implements provider-agnostic model invocation contracts.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface ILlmClient
{

    /**
     * Performs chat as part of ILlmClient runtime responsibilities.
     * @param request The request used by this operation.
     * @return The value produced by this operation.
     */
    LlmResponse chat(LlmRequest request);

    /**
     * Performs chat stream as part of ILlmClient runtime responsibilities.
     * @param request The request used by this operation.
     * @param listener The listener used by this operation.
     * @return The value produced by this operation.
     */
    default LlmResponse chatStream(LlmRequest request, LlmStreamListener listener)
    {
        LlmResponse response = chat(request);
        if (listener != null && response != null)
        {
            if (!response.getContent().isBlank())
            {
                listener.onDelta(response.getContent());
            }
            for (var toolCall : response.getToolCalls())
            {
                listener.onToolCall(toolCall);
            }
            if (response.getTokenUsage() != null)
            {
                listener.onTokenUsage(response.getTokenUsage());
            }
            response.getHandoffAgentId().ifPresent(listener::onHandoff);
            listener.onCompleted(response);
        }
        return response;
    }
}
